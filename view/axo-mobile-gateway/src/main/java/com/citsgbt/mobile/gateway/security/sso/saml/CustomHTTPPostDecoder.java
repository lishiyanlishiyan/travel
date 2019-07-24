package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.gateway.security.sso.SsoLoginResultVo;
import com.citsgbt.mobile.gateway.security.sso.utils.Keys;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;
import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.transport.InTransport;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 修改保留url中的参数
 *
 * @author gary.fu
 */
class CustomHTTPPostDecoder extends HTTPPostDecoder {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public CustomHTTPPostDecoder() {
	}

	public CustomHTTPPostDecoder(ParserPool pool) {
		super(pool);
	}

	@Override
	protected String getActualReceiverEndpointURI(SAMLMessageContext messageContext) throws MessageDecodingException {
		InTransport inTransport = messageContext.getInboundMessageTransport();
		if (!(inTransport instanceof HttpServletRequestAdapter)) {
			logger.error("Message context InTransport instance was an unsupported type: {}",
					inTransport.getClass().getName());
			throw new MessageDecodingException("Message context InTransport instance was an unsupported type");
		}
		HttpServletRequest httpRequest = ((HttpServletRequestAdapter) inTransport).getWrappedRequest();
		String url = HttpRequestUtils.getRequestUrl(httpRequest);
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof SAMLConfig && StringUtils.isNotBlank(((SAMLConfig) loginConfig).getAssertionConsumerServiceURL())) {
			url = ((SAMLConfig) loginConfig).getAssertionConsumerServiceURL();
		}
		return url;
	}

	@Override
	protected void doDecode(MessageContext messageContext) throws MessageDecodingException {
		processSsoLoginResult();
		super.doDecode(messageContext);
	}

	/**
	 * 解析SAMLResponse结果
	 */
    private void processSsoLoginResult() {
		HttpServletRequest request = HttpRequestUtils.getCurrentRequest();
		if (request != null) {
			LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
			if (loginConfig instanceof SAMLConfig) {
				SAMLConfig samlConfig = (SAMLConfig) loginConfig;
				Map<String, Object> contentMap = HttpRequestUtils.processInputMap(request);
				SsoLoginResultVo loginResultVo = new SsoLoginResultVo(samlConfig, contentMap, new LoginVo(loginConfig.getCompanyCode()));
				loginResultVo.setIpAddress(HttpRequestUtils.getIp(request));
				loginResultVo.setResponse(request.getParameter("SAMLResponse"));
				request.setAttribute(Keys.SSO_LOGIN_RESULT_KEY, loginResultVo);
			}
		}
	}

	@Override
	protected void populateRelyingPartyMetadata(SAMLMessageContext messageContext) throws MessageDecodingException {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof SAMLConfig) {
			SAMLConfig samlConfig = (SAMLConfig) loginConfig;
			if (StringUtils.isBlank(samlConfig.getEntityId()) && StringUtils.isBlank(samlConfig.getMetadataPath())) {
				String entityId = SSOUtils.getPeerEntityId(loginConfig, null);
				messageContext.setInboundMessageIssuer(entityId);
			}
		}
		super.populateRelyingPartyMetadata(messageContext);
	}

	@Override
	protected void processSecurityPolicy(MessageContext messageContext) throws SecurityException {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof SAMLConfig) {
			SAMLConfig samlConfig = (SAMLConfig) loginConfig;
			if (BooleanUtils.toBoolean(samlConfig.getVerifySignatureNeeded())) {
				super.processSecurityPolicy(messageContext);
			}
		}
	}

	/**
	 * 不校验请求URL地址
	 */
	@Override
	protected boolean compareEndpointURIs(String messageDestination, String receiverEndpoint) {
		return true;
	}
}

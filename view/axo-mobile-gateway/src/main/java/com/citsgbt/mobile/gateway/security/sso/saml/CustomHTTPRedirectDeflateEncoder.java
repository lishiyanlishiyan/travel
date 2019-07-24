package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.gateway.security.sso.SsoLoginResultVo;
import com.citsgbt.mobile.gateway.security.sso.event.SsoLoginEvent;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;
import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.encoding.HTTPRedirectDeflateEncoder;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * redirect因为使用GET方式默认不做签名,修改成支持签名方式，但是可能浏览器地址栏会超长
 */
class CustomHTTPRedirectDeflateEncoder extends HTTPRedirectDeflateEncoder {

	private static final Logger logger = LoggerFactory.getLogger(CustomHTTPRedirectDeflateEncoder.class);

	@Override
	protected void removeSignature(SAMLMessageContext messageContext) {
		try {
			signMessage(messageContext);
		} catch (MessageEncodingException e) {
			logger.error("sign error", e);
		}
	}

	@Override
	protected String buildRedirectURL(SAMLMessageContext messagesContext, String endpointURL, String message) throws MessageEncodingException {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof SAMLConfig) {
			SAMLConfig samlConfig = (SAMLConfig) loginConfig;
			Map<String, Object> contentMap = new HashMap<>();
			contentMap.put("SAMLRequest", message);
			contentMap.put("Destination", endpointURL);
			SsoLoginResultVo loginResultVo = new SsoLoginResultVo(loginConfig, contentMap, new LoginVo(samlConfig.getCompanyCode()));
			loginResultVo.setIpAddress(HttpRequestUtils.getClientIp());
			loginResultVo.setRequest(message);
			SsoLoginEvent.success(loginResultVo).publish();
		}
		return super.buildRedirectURL(messagesContext, endpointURL, message);
	}
}

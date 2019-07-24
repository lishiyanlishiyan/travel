package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.gateway.security.sso.SsoLoginResultVo;
import com.citsgbt.mobile.gateway.security.sso.event.SsoLoginEvent;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;
import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.saml2.binding.encoding.HTTPPostEncoder;
import org.opensaml.ws.message.encoder.MessageEncodingException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gary.fu
 */
class CustomHTTPPostEncoder extends HTTPPostEncoder {
	/**
	 * Constructor.
	 *
	 * @param engine     Velocity engine instance used to create POST body
	 * @param templateId ID of the template used to create POST body
	 */
	public CustomHTTPPostEncoder(VelocityEngine engine, String templateId) {
		super(engine, templateId);
	}

	@Override
	protected void populateVelocityContext(VelocityContext velocityContext, SAMLMessageContext messageContext, String endpointURL) throws MessageEncodingException {
		super.populateVelocityContext(velocityContext, messageContext, endpointURL);
		String requestStr = (String) velocityContext.get("SAMLRequest");
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof SAMLConfig) {
			SAMLConfig samlConfig = (SAMLConfig) loginConfig;
			Map<String, Object> contentMap = new HashMap<>();
			contentMap.put("SAMLRequest", requestStr);
			contentMap.put("Destination", endpointURL);
			SsoLoginResultVo loginResultVo = new SsoLoginResultVo(loginConfig, contentMap, new LoginVo(samlConfig.getCompanyCode()));
			loginResultVo.setIpAddress(HttpRequestUtils.getClientIp());
			loginResultVo.setRequest(requestStr);
			SsoLoginEvent.success(loginResultVo).publish();
		}
	}
}

package com.citsgbt.mobile.gateway.security.sso.cas;

import org.jasig.cas.client.util.CommonUtils;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomCasAuthenticationEntryPoint extends CasAuthenticationEntryPoint {

	@Override
	protected String createServiceUrl(HttpServletRequest request, HttpServletResponse response) {
		return CommonUtils.constructServiceUrl(request, response,
				null, getServiceProperties().getService(),
				getServiceProperties().getServiceParameter(),
				this.getServiceProperties().getArtifactParameter(),
				this.getEncodeServiceUrlWithSessionId());
	}
}

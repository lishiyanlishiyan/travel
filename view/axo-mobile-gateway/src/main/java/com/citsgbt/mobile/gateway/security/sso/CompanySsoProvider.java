package com.citsgbt.mobile.gateway.security.sso;

import com.citsamex.app.spi.data.caller.response.company.CompanySsoConfigResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface CompanySsoProvider {

	/**
	 * 获取公司SSO配置
	 *
	 * @param companyCode
	 * @return
	 */
	CompanySsoConfigResponse getSsoConfigResponse(String companyCode);

	/**
	 * build oauth认证URL地址
	 *
	 * @param clientId
	 * @param redirectUri
	 * @return
	 */
	String buildRedirectUri(String clientId, String redirectUri);

	/**
	 * 存储参数以及redirect
	 *
	 * @param request
	 * @param response
	 * @param clientId
	 * @param redirectUri
	 * @throws IOException
	 */
	void storeStatusAndRedirect(HttpServletRequest request, HttpServletResponse response, String clientId, String redirectUri) throws IOException;
}

package com.citsgbt.mobile.gateway.security.sso;

import com.citsamex.app.spi.data.dto.base.CommonDto;
import com.citsgbt.mobile.core.utils.json.CustomJsonUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;

import java.util.Map;

public class SsoLoginResultVo extends CommonDto {

	private LoginVo loginVo;

	private LoginConfig loginConfig;

	private String ipAddress;

	private String content;

	private String request;

	private String response;

	public SsoLoginResultVo(LoginConfig loginConfig, String content) {
		this.loginConfig = loginConfig;
		this.content = content;
	}

	public SsoLoginResultVo(LoginConfig loginConfig, String content, LoginVo loginVo) {
		this.loginVo = loginVo;
		this.loginConfig = loginConfig;
		this.content = content;
	}

	public SsoLoginResultVo(LoginConfig loginConfig, Map<String, Object> contentMap, LoginVo loginVo) {
		this.loginVo = loginVo;
		this.loginConfig = loginConfig;
		this.content = CustomJsonUtils.toJson(contentMap);
	}

	public LoginVo getLoginVo() {
		return loginVo;
	}

	public void setLoginVo(LoginVo loginVo) {
		this.loginVo = loginVo;
	}

	public LoginConfig getLoginConfig() {
		return loginConfig;
	}

	public void setLoginConfig(LoginConfig loginConfig) {
		this.loginConfig = loginConfig;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
}

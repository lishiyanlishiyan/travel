package com.citsgbt.mobile.gateway.security.sso.vo;

public class CASConfig extends LoginConfig {

	private String casServerLoginUrl;
	private String casServerUrlPrefix;
	private String serverName;
	private String casLoginField;

	public String getCasServerLoginUrl() {
		return casServerLoginUrl;
	}

	public void setCasServerLoginUrl(String casServerLoginUrl) {
		this.casServerLoginUrl = casServerLoginUrl;
	}

	public String getCasServerUrlPrefix() {
		return casServerUrlPrefix;
	}

	public void setCasServerUrlPrefix(String casServerUrlPrefix) {
		this.casServerUrlPrefix = casServerUrlPrefix;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getCasLoginField() {
		return casLoginField;
	}

	public void setCasLoginField(String casLoginField) {
		this.casLoginField = casLoginField;
	}
}

package com.citsgbt.mobile.core.ws.config.sso;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sso.param.config")
public class SsoParamConfigProperties {

	private Integer ssoParamExpire = 10 * 60;

	private String ssoParamPrefix = "axo-mobile:sso-params:";

	public Integer getSsoParamExpire() {
		return ssoParamExpire;
	}

	public void setSsoParamExpire(Integer ssoParamExpire) {
		this.ssoParamExpire = ssoParamExpire;
	}

	public String getSsoParamPrefix() {
		return ssoParamPrefix;
	}

	public void setSsoParamPrefix(String ssoParamPrefix) {
		this.ssoParamPrefix = ssoParamPrefix;
	}
}

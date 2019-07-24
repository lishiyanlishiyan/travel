package com.citsgbt.mobile.gateway.security.sso.vo;

import com.citsamex.app.spi.data.dto.base.CommonDto;
import org.apache.commons.lang3.BooleanUtils;

public class LoginConfig extends CommonDto {

	private String systemKey = "AXO-MOBILE";
	private String companyCode;
	private String loginBeanId;
	private String tokenName;
	private String tokenSecret;
	private String allowNormalLogin;
	private String logoutURL;
	private String key;
	private String type;
	private String companyIp;
	private String validateIp;
	private boolean openSsoRegister;
	private boolean internalSso;
	private String internalClientId;
	private String redirectUri;
	private boolean forceLogout;

	public String getLoginBeanId() {
		return loginBeanId;
	}

	public void setLoginBeanId(String loginBeanId) {
		this.loginBeanId = loginBeanId;
	}

	public String getAllowNormalLogin() {
		return allowNormalLogin;
	}

	public void setAllowNormalLogin(String allowNormalLogin) {
		this.allowNormalLogin = allowNormalLogin;
	}

	public String getLogoutURL() {
		return logoutURL;
	}

	public void setLogoutURL(String logoutURL) {
		this.logoutURL = logoutURL;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public String getTokenName() {
		return tokenName;
	}

	public void setTokenName(String tokenName) {
		this.tokenName = tokenName;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isSsoOnly() {
		return !BooleanUtils.toBoolean(getAllowNormalLogin());
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCompanyIp() {
		return companyIp;
	}

	public void setCompanyIp(String companyIp) {
		this.companyIp = companyIp;
	}

	public String getValidateIp() {
		return validateIp;
	}

	public void setValidateIp(String validateIp) {
		this.validateIp = validateIp;
	}

	public boolean isOpenSsoRegister() {
		return openSsoRegister;
	}

	public void setOpenSsoRegister(boolean openSsoRegister) {
		this.openSsoRegister = openSsoRegister;
	}

	public boolean isInternalSso() {
		return internalSso;
	}

	public void setInternalSso(boolean internalSso) {
		this.internalSso = internalSso;
	}

	public String getInternalClientId() {
		return internalClientId;
	}

	public void setInternalClientId(String internalClientId) {
		this.internalClientId = internalClientId;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public String getSystemKey() {
		return systemKey;
	}

	public void setSystemKey(String systemKey) {
		this.systemKey = systemKey;
	}

	public boolean isForceLogout() {
		return forceLogout;
	}

	public void setForceLogout(boolean forceLogout) {
		this.forceLogout = forceLogout;
	}
}

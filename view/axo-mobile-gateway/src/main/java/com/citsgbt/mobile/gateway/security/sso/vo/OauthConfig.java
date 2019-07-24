package com.citsgbt.mobile.gateway.security.sso.vo;

/**
 * @author gary.fu
 */
public class OauthConfig extends LoginConfig {

	private String clientId;

	private String clientSecret;

	private String authorizationUri;

	private String tokenUri;

	private String scope;

	private String userInfoUri;

	private String authorizationGrantType;

	private String userIDAttrName;

	private String additionHeaders;

	private String oauthRedirectUri;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getAuthorizationUri() {
		return authorizationUri;
	}

	public void setAuthorizationUri(String authorizationUri) {
		this.authorizationUri = authorizationUri;
	}

	public String getTokenUri() {
		return tokenUri;
	}

	public void setTokenUri(String tokenUri) {
		this.tokenUri = tokenUri;
	}

	public String getUserInfoUri() {
		return userInfoUri;
	}

	public void setUserInfoUri(String userInfoUri) {
		this.userInfoUri = userInfoUri;
	}

	public String getAuthorizationGrantType() {
		return authorizationGrantType;
	}

	public void setAuthorizationGrantType(String authorizationGrantType) {
		this.authorizationGrantType = authorizationGrantType;
	}

	public String getUserIDAttrName() {
		return userIDAttrName;
	}

	public void setUserIDAttrName(String userIDAttrName) {
		this.userIDAttrName = userIDAttrName;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getAdditionHeaders() {
		return additionHeaders;
	}

	public void setAdditionHeaders(String additionHeaders) {
		this.additionHeaders = additionHeaders;
	}

	public String getOauthRedirectUri() {
		return oauthRedirectUri;
	}

	public void setOauthRedirectUri(String oauthRedirectUri) {
		this.oauthRedirectUri = oauthRedirectUri;
	}
}

package com.citsgbt.mobile.gateway.security.sso.saml;

import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLCredential;

import java.util.Date;

/**
 * @author gary.fu
 */
public class CustomSAMLAuthenticationProvider extends SAMLAuthenticationProvider {

	private boolean responseExpiration = false;

	/**
	 * 是否从response获取会话超时时间
	 *
	 * @param credential
	 * @return
	 */
	@Override
	protected Date getExpirationDate(SAMLCredential credential) {
		return isResponseExpiration() ? super.getExpirationDate(credential) : null;
	}

	private boolean isResponseExpiration() {
		return responseExpiration;
	}

	public void setResponseExpiration(boolean responseExpiration) {
		this.responseExpiration = responseExpiration;
	}
}

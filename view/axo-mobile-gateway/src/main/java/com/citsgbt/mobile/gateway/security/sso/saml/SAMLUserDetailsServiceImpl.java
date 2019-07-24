package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;
import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

	private UserDetailsService userDetailsService;

	private SAMLConfig samlConfig;

	public SAMLUserDetailsServiceImpl(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	public SAMLUserDetailsServiceImpl(UserDetailsService userDetailsService, SAMLConfig samlConfig) {
		this.userDetailsService = userDetailsService;
		this.samlConfig = samlConfig;
	}

	public SAMLUserDetailsServiceImpl() {
	}

	@Override
	public Object loadUserBySAML(SAMLCredential credential) {
		if (samlConfig != null) {
			String userID = credential.getNameID().getValue();
			String userName = new LoginVo(samlConfig.getCompanyCode(), userID).toUsername();
			return userDetailsService.loadUserByUsername(userName);
		}
		return null;
	}

	public UserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	public SAMLConfig getSamlConfig() {
		return samlConfig;
	}

	public void setSamlConfig(SAMLConfig samlConfig) {
		this.samlConfig = samlConfig;
	}
}

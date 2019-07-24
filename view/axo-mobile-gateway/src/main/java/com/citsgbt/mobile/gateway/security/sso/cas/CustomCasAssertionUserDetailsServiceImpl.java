package com.citsgbt.mobile.gateway.security.sso.cas;

import com.citsgbt.mobile.gateway.security.sso.cas.utils.CasParseUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.CASConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author gary.fu
 */
public class CustomCasAssertionUserDetailsServiceImpl extends AbstractCasAssertionUserDetailsService {

	private UserDetailsService userDetailsService;

	private CASConfig casConfig;

	public CustomCasAssertionUserDetailsServiceImpl() {
	}

	public CustomCasAssertionUserDetailsServiceImpl(UserDetailsService userDetailsService, CASConfig casConfig) {
		this.userDetailsService = userDetailsService;
		this.casConfig = casConfig;
	}

	@Override
	protected UserDetails loadUserDetails(Assertion assertion) {
		if (casConfig != null) {
			LoginVo loginVo = CasParseUtils.parseAssertion(assertion, casConfig);
			return userDetailsService.loadUserByUsername(loginVo.toUsername());
		}
		return null;
	}

	public UserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	public CASConfig getCasConfig() {
		return casConfig;
	}

	public void setCasConfig(CASConfig casConfig) {
		this.casConfig = casConfig;
	}
}

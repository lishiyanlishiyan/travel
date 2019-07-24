package com.citsgbt.mobile.gateway.config.security;

import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Created on 2014/8/8 17:27.<br>
 *
 * @author gary.fu
 */
public class CustomFormLoginConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractAuthenticationFilterConfigurer<H, CustomFormLoginConfigurer<H>, CustomUsernamePasswordAuthenticationFilter> {
	/**
	 * Creates a new instance
	 *
	 * @param authenticationFilter      the {@link org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter} to use
	 * @param defaultLoginProcessingUrl the default URL to use for {@link #loginProcessingUrl(String)}
	 */
    private CustomFormLoginConfigurer(CustomUsernamePasswordAuthenticationFilter authenticationFilter, String defaultLoginProcessingUrl) {
		super(authenticationFilter, defaultLoginProcessingUrl);
	}

	public CustomFormLoginConfigurer(CustomUsernamePasswordAuthenticationFilter authenticationFilter) {
		this(authenticationFilter, "/j_spring_security_check");
	}

	public CustomFormLoginConfigurer() {
		this(new CustomUsernamePasswordAuthenticationFilter());
	}

	@Override
	protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
		return new AntPathRequestMatcher(loginProcessingUrl, "POST");
	}

	@Override
	public CustomFormLoginConfigurer<H> loginPage(String loginPage) {
		return super.loginPage(loginPage);
	}

	public CustomFormLoginConfigurer<H> companyParameter(String companyParameter) {
		getAuthenticationFilter().setCompanyParameter(companyParameter);
		return this;
	}


	public CustomFormLoginConfigurer<H> usernameParameter(String usernameParameter) {
		getAuthenticationFilter().setUsernameParameter(usernameParameter);
		return this;
	}

	/**
	 * The HTTP parameter to look for the password when performing
	 * authentication. Default is "password".
	 *
	 * @param passwordParameter the HTTP parameter to look for the password when performing
	 *                          authentication
	 * @return the {@link FormLoginConfigurer} for additional customization
	 */
	public CustomFormLoginConfigurer<H> passwordParameter(String passwordParameter) {
		getAuthenticationFilter().setPasswordParameter(passwordParameter);
		return this;
	}
}

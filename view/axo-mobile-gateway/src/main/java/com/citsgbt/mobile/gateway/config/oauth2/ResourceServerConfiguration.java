package com.citsgbt.mobile.gateway.config.oauth2;

import com.citsgbt.mobile.gateway.consts.OAuth2Consts;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.web.cors.CorsUtils;

@Configuration
@EnableResourceServer
class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) {
		resources.resourceId(OAuth2Consts.AXO_RESOURCE_ID).stateless(true);
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				.and()
				.requestMatchers().antMatchers("/hello", "/axo-mobile-open/**", "/onlineSso")
				.and()
				.cors()
				.and()
				.authorizeRequests()
				.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
				.antMatchers("/hello").access("#oauth2.clientHasRole('ROLE_CLIENT')")
				.antMatchers("/axo-mobile-open/login", "/axo-mobile-open/redirect", "/onlineSso").permitAll()
				.antMatchers("/axo-mobile-open/**").access("#oauth2.isUser()");
	}

}

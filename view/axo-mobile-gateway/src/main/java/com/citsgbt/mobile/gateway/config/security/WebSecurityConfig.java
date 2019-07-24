/**
 *
 */
package com.citsgbt.mobile.gateway.config.security;

import com.citsgbt.mobile.gateway.security.sso.CustomSsoAuthenticationFailureHandler;
import com.citsgbt.mobile.gateway.security.sso.CustomSsoAuthenticationFilter;
import com.citsgbt.mobile.gateway.security.sso.CustomSsoAuthenticationSuccessHandler;
import com.citsgbt.mobile.gateway.web.filters.LocaleRequestContextFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.LocaleResolver;

/**
 * @author gary.fu
 */
//@Configuration
//@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	protected UserDetailsService userDetailsServiceLocal;

	@Autowired(required = false)
	protected LocaleResolver localeResolver;

	@Autowired
	protected LocaleRequestContextFilter localeRequestContextFilter;

	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public DaoAuthenticationProvider daoAuthenticationProvider() {
		LocaleDaoAuthenticationProvider provider = new LocaleDaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsServiceLocal);
		provider.setHideUserNotFoundExceptions(false);
        provider.setPasswordEncoder(new Sha512WithSaltPasswordEncoder()); // 自定义的password盐值处理规则
		return provider;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		CustomFormLoginConfigurer<HttpSecurity> customFormLoginConfigurer
				= new CustomFormLoginConfigurer<HttpSecurity>(new CustomUsernamePasswordAuthenticationFilter(localeResolver))
				.defaultSuccessUrl("/index", false);
		// 字符编码Filter
		CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
		encodingFilter.setEncoding("UTF-8");
		encodingFilter.setForceEncoding(true);
		http.apply(customFormLoginConfigurer);
		http.rememberMe().key("new-axo-mobile-remember-me").rememberMeCookieName("new-axo-mobile-remember-me")
				.userDetailsService(userDetailsServiceLocal).and() // 启用记住登录
				.anonymous().and()
				.csrf().disable()
				.formLogin()
				.loginPage("/login")
				.loginProcessingUrl("/j_spring_security_check").and()
				.logout().and()
				.authorizeRequests().antMatchers(
				"/dynamic/js/**",
				"/html/**",
				"/approve**",
				"/login/**",
				"/ssoLoginConfig/**",
				"/oauth/token",
				"/axo-mobile-open/login**",
				"/axo-mobile-open/redirect",
				"/axo-mobile/**").permitAll().and()
				.authorizeRequests().anyRequest().authenticated().and()
				.sessionManagement()
				.sessionFixation()
				.migrateSession()
				.maximumSessions(10)
				.expiredUrl("/login")
//				.sessionRegistry(sessionRegistry)
				.and().and()
				// 没有权限跳转页面
				.exceptionHandling().accessDeniedPage("/accessDenied").and()
				.addFilterAfter(customSsoAuthenticationFilter(), LogoutFilter.class)
				.addFilterBefore(encodingFilter, SecurityContextPersistenceFilter.class)
				.addFilterBefore(localeRequestContextFilter, SecurityContextPersistenceFilter.class);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		super.configure(web);
		// 设置debug模式
		web.debug(true);
		// 去掉部分安全性控制
		web.ignoring().antMatchers("/login.html",
				"/js/**", "/images/**", "/font/**", "/themes/**",
				"/css/**", "/i18n/**", "/vendor/**", "/webjars/**");
	}

	@Bean
	public CustomSsoAuthenticationFilter customSsoAuthenticationFilter() throws Exception {
		CustomSsoAuthenticationFilter customSsoAuthenticationFilter = new CustomSsoAuthenticationFilter();
		customSsoAuthenticationFilter.setAuthenticationManager(authenticationManager());
		customSsoAuthenticationFilter.setAuthenticationFailureHandler(customSsoAuthenticationFailureHandler());
		customSsoAuthenticationFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler());
		return customSsoAuthenticationFilter;
	}

	@Bean
	public CustomSsoAuthenticationFailureHandler customSsoAuthenticationFailureHandler() {
		return new CustomSsoAuthenticationFailureHandler();
	}

	@Bean
	public CustomSsoAuthenticationSuccessHandler customAuthenticationSuccessHandler() {
		CustomSsoAuthenticationSuccessHandler customSsoAuthenticationSuccessHandler = new CustomSsoAuthenticationSuccessHandler();
		customSsoAuthenticationSuccessHandler.setAlwaysUseDefaultTargetUrl(true);
		customSsoAuthenticationSuccessHandler.setDefaultTargetUrl("/index");
		return customSsoAuthenticationSuccessHandler;
	}

	@Bean
	public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider() {
		PreAuthenticatedAuthenticationProvider authenticatedAuthenticationProvider = new PreAuthenticatedAuthenticationProvider();
		authenticatedAuthenticationProvider.setPreAuthenticatedUserDetailsService(new UserDetailsByNameServiceWrapper<>(userDetailsServiceLocal));
		return authenticatedAuthenticationProvider;
	}

}

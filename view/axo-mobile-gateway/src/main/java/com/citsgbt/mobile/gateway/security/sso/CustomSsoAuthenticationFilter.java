package com.citsgbt.mobile.gateway.security.sso;

import com.citsamex.app.spi.data.caller.response.company.CompanySsoConfigResponse;
import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.core.utils.spring.ApplicationContextUtils;
import com.citsgbt.mobile.core.utils.spring.MessageSourceUtils;
import com.citsgbt.mobile.gateway.security.sso.exception.SsoException;
import com.citsgbt.mobile.gateway.security.sso.oauth.CustomOAuth2AuthorizationRequestRedirectFilter;
import com.citsgbt.mobile.gateway.security.sso.utils.Keys;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.*;
import com.citsgbt.mobile.gateway.security.user.CustomUserDetails;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 所有sso单点登陆filter
 * <p>
 * 针对SAML和CAS以及WebService模式sso登陆
 * Created by gary.fu on 2018/8/10.
 */
public class CustomSsoAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

	private static final Logger loggerLocal = LoggerFactory.getLogger(CustomSsoAuthenticationFilter.class);

	@Autowired
	private CompanySsoProvider companySsoProvider;

	private String filterProcessUrl = "/sso/**";

	private RequestMatcher requiresAuthenticationRequestMatcher;

	@Autowired
	private CasAuthenticationFilter casAuthenticationFilter;

	@Autowired
	@Qualifier("samlWebSSOProcessingFilter")
	private SAMLProcessingFilter samlProcessingFilter;

	@Autowired
	private CustomOAuth2AuthorizationRequestRedirectFilter customOAuth2AuthorizationRequestRedirectFilter;

	@Autowired
	private OAuth2LoginAuthenticationFilter oAuth2LoginAuthenticationFilter;

	private LogoutHandler logoutHandler = new SecurityContextLogoutHandler();

	/**
	 * jwt路径
	 */
	private RequestMatcher jwtRequestMatcher = new AntPathRequestMatcher(SSOUtils.SSO_PATH_JWT);

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		List<RequestMatcher> requestMatchers = new ArrayList<>();
		requestMatchers.add(new AntPathRequestMatcher(getFilterProcessUrl()));
		requiresAuthenticationRequestMatcher = new OrRequestMatcher(requestMatchers);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		SSOUtils.setCurrentLoginConfig(null);
		String companyId = SSOUtils.populateCompanyId(request);

		CommonConfig jwtConfig = checkJwtSso((HttpServletRequest) request);
		if (jwtConfig != null) {
			loggerLocal.info("JWT单点登陆{}", jwtConfig.getCompanyCode());
			SSOUtils.setCurrentLoginConfig(jwtConfig);
			doCommonLogin(request, response, chain, jwtConfig);
		} else if (requiresAuthenticationRequestMatcher.matches((HttpServletRequest) request) && StringUtils.isNotBlank(companyId)) {
			doSsoLogin(request, response, chain);
		} else {
			chain.doFilter(request, response);
		}
	}

	protected void doSsoLogin(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String companyId = SSOUtils.populateCompanyId(request);
		String key = request.getParameter("ssoKey");
		String requestPath = HttpRequestUtils.getRequestPath((HttpServletRequest) request);
		SsoType ssoType = getSsoType(requestPath);
		CompanySsoConfigResponse ssoResponse = companySsoProvider.getSsoConfigResponse(companyId);
		loggerLocal.info("单点登陆{}/{}/{}", companyId, ssoType, key);
		LoginConfig loginConfig = SSOUtils.getLoginConfig(ssoResponse, key, ssoType);
		if (loginConfig != null) {
			SSOUtils.setCurrentLoginConfig(loginConfig);
			checkForceLogout((HttpServletRequest) request, (HttpServletResponse) response, loginConfig);
			if (loginConfig instanceof CASConfig) {
				casAuthenticationFilter.doFilter(request, response, chain);
			} else if (loginConfig instanceof CommonConfig) {
				doCommonLogin(request, response, chain, loginConfig);
			} else if (loginConfig instanceof SAMLConfig) {
				samlProcessingFilter.doFilter(request, response, chain);
			} else if (loginConfig instanceof OauthConfig) {
				if (SSOUtils.isOauthResponse((HttpServletRequest) request)) {
					oAuth2LoginAuthenticationFilter.doFilter(request, response, chain);
				} else {
					customOAuth2AuthorizationRequestRedirectFilter.doFilter(request, response, chain);
				}
			} else {
				unsuccessfulAuthentication((HttpServletRequest) request, (HttpServletResponse) response,
						new SsoException(MessageSourceUtils.getMessage("sso.invalid.assert.empty"), loginConfig));
			}
		} else {
			LoginConfig errorConfig = new LoginConfig();
			errorConfig.setCompanyCode(companyId);
			errorConfig.setType(ssoType == null ? null : ssoType.getValue());
			errorConfig.setKey(key);
			unsuccessfulAuthentication((HttpServletRequest) request, (HttpServletResponse) response,
					new SsoException(MessageSourceUtils.getMessage("sso.nocompany.config"),
							errorConfig));
		}
	}

	private void checkForceLogout(HttpServletRequest request, HttpServletResponse response, LoginConfig loginConfig) {
		if (loginConfig.isForceLogout() && isLogin()) {
			logoutHandler.logout(request, response,
					SecurityContextHolder.getContext().getAuthentication());
		}
	}

	private void doCommonLogin(ServletRequest request, ServletResponse response, FilterChain chain, LoginConfig loginConfig) throws IOException, ServletException {
		try {
			String loginBeanId = loginConfig.getLoginBeanId();
			Login login = ApplicationContextUtils.getBean(loginBeanId, Login.class);
			LoginVo loginVo = login.login((HttpServletRequest) request, (HttpServletResponse) response, loginConfig);
			if (loginVo == null) {
				loginVo = new LoginVo(loginConfig.getCompanyCode());
			}
			request.setAttribute(Keys.SSO_LOGIN_VO_KEY, loginVo);
			if (StringUtils.isBlank(loginVo.getLoginName())) {
				throw new SsoException(MessageSourceUtils.getMessage("sso.invalid.no.principal"), loginConfig);
			}
		} catch (AuthenticationException e) {
			unsuccessfulAuthentication((HttpServletRequest) request, (HttpServletResponse) response, e);
		}
		super.doFilter(request, response, chain);
	}

	/**
	 * JWT sso
	 *
	 * @param request
	 * @return
	 */
	private CommonConfig checkJwtSso(HttpServletRequest request) {
		String accessToken = request.getParameter(SSOUtils.JWT_ACCESS_TOKEN_KEY);
		String companyId = SSOUtils.populateCompanyId(request);
		CommonConfig jwtConfig = null;
		if (StringUtils.isNotBlank(accessToken) && StringUtils.isBlank(companyId)) { // 没有公司ID的时候
			try {
				SignedJWT signedJWT = SignedJWT.parse(accessToken);
				JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
				String companyCode = jwtClaimsSet.getStringClaim("companyCode");
				if (StringUtils.isNotBlank(companyCode)) {
					jwtConfig = new CommonConfig();
					jwtConfig.setCompanyCode(companyCode);
					jwtConfig.setLoginBeanId("jwtLogin");
					jwtConfig.setType(SsoType.JWT.getValue());
					jwtConfig.setInternalSso(!jwtRequestMatcher.matches(request));
				}
			} catch (ParseException e) {
				loggerLocal.error("解析JwtToken错误", e);
			}
		}
		return jwtConfig;
	}

	private SsoType getSsoType(String requestPath) {
		return SSOUtils.getSsoTypeByPath(requestPath);
	}

	private boolean isLogin(){
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.getPrincipal() instanceof CustomUserDetails;
	}

	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		LoginVo loginVo = (LoginVo) request.getAttribute(Keys.SSO_LOGIN_VO_KEY);
		return loginVo == null ? null : loginVo.toUsername();
	}

	@Override
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		return "N/A";
	}

	private String getFilterProcessUrl() {
		return filterProcessUrl;
	}

	public void setFilterProcessUrl(String filterProcessUrl) {
		this.filterProcessUrl = filterProcessUrl;
	}

	public RequestMatcher getRequiresAuthenticationRequestMatcher() {
		return requiresAuthenticationRequestMatcher;
	}

	public void setRequiresAuthenticationRequestMatcher(RequestMatcher requiresAuthenticationRequestMatcher) {
		this.requiresAuthenticationRequestMatcher = requiresAuthenticationRequestMatcher;
	}

	public RequestMatcher getJwtRequestMatcher() {
		return jwtRequestMatcher;
	}

	public void setJwtRequestMatcher(RequestMatcher jwtRequestMatcher) {
		this.jwtRequestMatcher = jwtRequestMatcher;
	}

	public LogoutHandler getLogoutHandler() {
		return logoutHandler;
	}

	public void setLogoutHandler(LogoutHandler logoutHandler) {
		this.logoutHandler = logoutHandler;
	}
}

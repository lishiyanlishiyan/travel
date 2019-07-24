package com.citsgbt.mobile.gateway.security.sso;

import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.core.utils.spring.ApplicationContextUtils;
import com.citsgbt.mobile.gateway.security.sso.cas.CustomCasAuthenticationEntryPoint;
import com.citsgbt.mobile.gateway.security.sso.event.SsoLoginEvent;
import com.citsgbt.mobile.gateway.security.sso.exception.SsoException;
import com.citsgbt.mobile.gateway.security.sso.utils.Keys;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomSsoAuthenticationFailureHandler implements AuthenticationFailureHandler {

	private static final Logger logger = LoggerFactory.getLogger(CustomSsoAuthenticationFailureHandler.class);

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
		logger.error("sso失败", e);
		request.getSession().invalidate();
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (e instanceof SsoException || loginConfig instanceof CommonConfig) {
			ssoFailed(request, response, loginConfig, e);
		} else if (loginConfig instanceof CASConfig) {
			if (StringUtils.isNotBlank(request.getParameter(ServiceProperties.DEFAULT_CAS_ARTIFACT_PARAMETER))) {
				ssoLoginFailedLog(request, getSsoException(e, loginConfig));
			}
			CASConfig casConfig = (CASConfig) loginConfig;
			ServiceProperties serviceProperties = new ServiceProperties();
			serviceProperties.setService(((CASConfig) loginConfig).getServerName());
			CustomCasAuthenticationEntryPoint casAuthenticationEntryPoint = new CustomCasAuthenticationEntryPoint();
			casAuthenticationEntryPoint.setLoginUrl(casConfig.getCasServerLoginUrl());
			casAuthenticationEntryPoint.setServiceProperties(serviceProperties);
			casAuthenticationEntryPoint.commence(request, response, e);
		} else if (loginConfig instanceof SAMLConfig) {
			if (StringUtils.isNotBlank(request.getParameter("SAMLResponse"))) {
				ssoFailed(request, response, loginConfig, e);
			} else {
				SAMLEntryPoint samlEntryPoint = ApplicationContextUtils.getBean(SAMLEntryPoint.class);
				try {
					samlEntryPoint.commence(request, response, e);
				} catch (SsoException e1) {
					ssoFailed(request, response, loginConfig, e1);
				} catch (Exception e1) {
					ssoFailed(request, response, loginConfig, new SsoException(e1.getMessage(), e1, loginConfig));
				}
			}
		} else if (loginConfig instanceof OauthConfig) {
			ssoFailed(request, response, loginConfig, e);
		}
	}

	private SsoException getSsoException(AuthenticationException e, LoginConfig loginConfig) {
		if (e instanceof SsoException) {
			return (SsoException) e;
		}
		return new SsoException(e.getMessage(), e, loginConfig);
	}

	/**
	 * sso失败
	 *
	 * @param request
	 * @param response
	 * @param loginConfig
	 * @param e
	 * @throws IOException
	 * @throws ServletException
	 */
	private void ssoFailed(HttpServletRequest request, HttpServletResponse response, LoginConfig loginConfig, AuthenticationException e) throws IOException, ServletException {
		SsoException exception = getSsoException(e, loginConfig);
		ssoLoginFailedLog(request, exception);
		if (loginConfig != null && loginConfig.isInternalSso()) {
			// noop
		} else {
			request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, exception);
			request.getRequestDispatcher("/errorPage").forward(request, response);
			logger.error("认证失败", e);
		}
	}

	/**
	 * 记录错误日志
	 *
	 * @param request
	 * @param e
	 * @return
	 */
	private SsoLoginResultVo ssoLoginFailedLog(HttpServletRequest request, SsoException e) {
		SsoLoginResultVo resultVo = (SsoLoginResultVo) request.getAttribute(Keys.SSO_LOGIN_RESULT_KEY);
		if (resultVo == null) {
			resultVo = new SsoLoginResultVo(e.getLoginConfig(), HttpRequestUtils.processInputMap(request), null);
			resultVo.setIpAddress(HttpRequestUtils.getIp(request));
		}
		SsoLoginEvent.error(resultVo, e).publish();
		return resultVo;
	}
}

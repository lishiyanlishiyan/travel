package com.citsgbt.mobile.gateway.config.security;

import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created on 2014/8/7 16:42.<br>
 *
 * @author gary.fu
 */
class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	/**
	 * 公司代码
	 */
	private static final String SPRING_SECURITY_FORM_COMPANY_CODE_KEY = "companyCode";

	private String companyParameter = SPRING_SECURITY_FORM_COMPANY_CODE_KEY;

	private LocaleResolver localeResolver;

	public CustomUsernamePasswordAuthenticationFilter(LocaleResolver localeResolver) {
		super();
		this.localeResolver = localeResolver;
	}

	public CustomUsernamePasswordAuthenticationFilter() {
		super();
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		if (localeResolver != null) {
			req.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, localeResolver); // 处理locale，和spring mvc结合
			if (req instanceof HttpServletRequest) {
				LocaleContextHolder.setLocale(HttpRequestUtils.getLocale((HttpServletRequest) req));
			}
		}
		super.doFilter(req, res, chain);
	}

	@Override
	protected String obtainUsername(HttpServletRequest request) {
		String username = super.obtainUsername(request);
		String companyCode = obtainCompanyCode(request);
		if (companyCode == null) {
			companyCode = "";
		}
		if (username == null) {
			username = "";
		}
		return StringUtils.join(Arrays.asList(companyCode, username), ",");
	}

	private String obtainCompanyCode(HttpServletRequest request) {
		return request.getParameter(getCompanyParameter());
	}

	private String getCompanyParameter() {
		return companyParameter;
	}

	public void setCompanyParameter(String companyParameter) {
		this.companyParameter = companyParameter;
	}

	public LocaleResolver getLocaleResolver() {
		return localeResolver;
	}

	public void setLocaleResolver(LocaleResolver localeResolver) {
		this.localeResolver = localeResolver;
	}
}

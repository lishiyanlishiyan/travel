package com.citsgbt.mobile.gateway.web.filters;

import com.citsgbt.mobile.core.spi.AxoLoginConsts;
import com.citsgbt.mobile.gateway.security.user.CustomUserDetails;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE;

@Component
class CustomSecurityHeaderFilter extends ZuulFilter {

	private static final String SEND_FORWARD_FILTER_RAN = "customSecurityHeaderFilter.ran";

	private String axoLoginId = AxoLoginConsts.AXO_LOGIN_ID;

	private String axoLoginCompanyCode = AxoLoginConsts.AXO_LOGIN_COMPANY_CODE;

	private String axoLoginName = AxoLoginConsts.AXO_LOGIN_NAME;

	private String axoLoginToken = AxoLoginConsts.AXO_LOGIN_TOKEN;

	@Override
	public String filterType() {
		return ROUTE_TYPE;
	}

	@Override
	public int filterOrder() {
		return PRE_DECORATION_FILTER_ORDER;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext ctx = RequestContext.getCurrentContext();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.getPrincipal() != null && !ctx.getBoolean(SEND_FORWARD_FILTER_RAN, false);
	}

	@Override
	public Object run() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		RequestContext ctx = RequestContext.getCurrentContext();
		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			ctx.addZuulRequestHeader(getAxoLoginId(), userDetails.getAccountDto().getUserId());
			ctx.addZuulRequestHeader(getAxoLoginCompanyCode(), userDetails.getAccountDto().getCompanyCode());
			ctx.addZuulRequestHeader(getAxoLoginName(), userDetails.getUsername());
			if (authentication.getDetails() instanceof OAuth2AuthenticationDetails) {
				ctx.addZuulRequestHeader(getAxoLoginToken(), ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue());
			}
		}
		return null;
	}

	private String getAxoLoginId() {
		return axoLoginId;
	}

	public void setAxoLoginId(String axoLoginId) {
		this.axoLoginId = axoLoginId;
	}

	private String getAxoLoginName() {
		return axoLoginName;
	}

	public void setAxoLoginName(String axoLoginName) {
		this.axoLoginName = axoLoginName;
	}

	private String getAxoLoginToken() {
		return axoLoginToken;
	}

	public void setAxoLoginToken(String axoLoginToken) {
		this.axoLoginToken = axoLoginToken;
	}

	public String getAxoLoginCompanyCode() {
		return axoLoginCompanyCode;
	}

	public void setAxoLoginCompanyCode(String axoLoginCompanyCode) {
		this.axoLoginCompanyCode = axoLoginCompanyCode;
	}
}

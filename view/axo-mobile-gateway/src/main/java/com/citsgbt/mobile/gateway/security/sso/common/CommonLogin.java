package com.citsgbt.mobile.gateway.security.sso.common;

import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class CommonLogin extends AbstractLogin {

	@Override
	public LoginVo doLogin(HttpServletRequest request, HttpServletResponse response, LoginConfig loginConfig) {
		String companyCode = SSOUtils.populateCompanyId(request);
		String tokenName = loginConfig.getTokenName();
		tokenName = StringUtils.isBlank(tokenName) ? "userId" : tokenName;
		String loginName = request.getParameter(tokenName);
		return new LoginVo(companyCode, loginName);
	}

	@Override
	protected boolean isNeedValidateIp(LoginConfig loginConfig) {
		if (StringUtils.isNotBlank(loginConfig.getValidateIp())) {
			return BooleanUtils.toBoolean(loginConfig.getValidateIp());
		}
		return true;
	}
}

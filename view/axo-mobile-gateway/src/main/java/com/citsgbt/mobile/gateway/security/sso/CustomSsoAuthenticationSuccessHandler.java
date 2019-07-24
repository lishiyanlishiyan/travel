package com.citsgbt.mobile.gateway.security.sso;

import com.citsgbt.mobile.gateway.security.sso.event.SsoLoginEvent;
import com.citsgbt.mobile.gateway.security.sso.utils.Keys;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;
import com.citsgbt.mobile.gateway.security.user.CustomUserDetails;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 单点登陆成功处理
 *
 * @author gary.fu
 */
public class CustomSsoAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private static final Logger loggerLocal = LoggerFactory.getLogger(CustomSsoAuthenticationSuccessHandler.class);

	@Autowired
	private CompanySsoProvider companySsoProvider;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig != null) {
			loggerLocal.info("{}", loginConfig);
			SsoLoginResultVo resultVo = (SsoLoginResultVo) request.getAttribute(Keys.SSO_LOGIN_RESULT_KEY);
			if ((resultVo.getLoginVo() == null || StringUtils.isBlank(resultVo.getLoginVo().getLoginName()))
					&& authentication.getPrincipal() instanceof CustomUserDetails) {
				String loginName = ((CustomUserDetails) authentication.getPrincipal()).getLoginName();
				resultVo.setLoginVo(new LoginVo(loginConfig.getCompanyCode(), loginName));
			}
			SsoLoginEvent.success(resultVo).publish();
			if (loginConfig.isInternalSso()) {
				return;
			}
			if (!response.isCommitted()) {
				companySsoProvider.storeStatusAndRedirect(request, response, loginConfig.getInternalClientId(), loginConfig.getRedirectUri());
				return;
			}
		}
		super.onAuthenticationSuccess(request, response, authentication);
	}
}

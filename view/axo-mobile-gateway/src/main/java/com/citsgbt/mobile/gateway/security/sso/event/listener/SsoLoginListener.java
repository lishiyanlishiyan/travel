package com.citsgbt.mobile.gateway.security.sso.event.listener;

import com.citsamex.app.spi.data.caller.request.log.SsoLoginLogParam;
import com.citsamex.app.spi.data.dto.log.SsoLoginLogDto;
import com.citsamex.app.spi.interfaces.targets.log.LoginLogTargetService;
import com.citsgbt.mobile.core.utils.json.CustomJsonUtils;
import com.citsgbt.mobile.gateway.security.sso.SsoLoginResultVo;
import com.citsgbt.mobile.gateway.security.sso.event.SsoLoginEvent;
import com.citsgbt.mobile.gateway.security.sso.event.SsoLoginFailureEvent;
import com.citsgbt.mobile.gateway.security.sso.event.SsoLoginSuccessEvent;
import com.citsgbt.mobile.gateway.security.sso.exception.SsoException;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 保存sso日志等
 *
 * @author gary.fu
 */
@Component
public class SsoLoginListener implements ApplicationListener<SsoLoginEvent> {

	private static final Logger logger = LoggerFactory.getLogger(SsoLoginListener.class);

	@Autowired
	private LoginLogTargetService loginLogTargetService;

	@Override
	public void onApplicationEvent(SsoLoginEvent event) {
		SsoLoginResultVo loginResult = event.getSsoLoginResult();
		LoginConfig loginConfig = loginResult.getLoginConfig();
		LoginVo loginVo = loginResult.getLoginVo();
		SsoLoginLogDto loginLogDto = new SsoLoginLogDto();
		loginLogDto.setCompanyCode(loginConfig.getCompanyCode());
		loginLogDto.setConfig(CustomJsonUtils.toJson(loginConfig));
		loginLogDto.setContent(loginResult.getContent());
		loginLogDto.setIp(loginResult.getIpAddress());
		loginLogDto.setIsError(event instanceof SsoLoginSuccessEvent ? 0 : 1);
		loginLogDto.setLoginName(loginVo != null ? loginVo.getLoginName() : null);
		loginLogDto.setLoginTime(new Date());
		loginLogDto.setSsoType(loginConfig.getType());
		if (event instanceof SsoLoginFailureEvent) {
			SsoException exception = ((SsoLoginFailureEvent) event).getException();
			if (exception != null) {
				loginLogDto.setException(ExceptionUtils.getStackTrace(exception));
			}
		}
		loginLogDto.setSamlResponse(loginResult.getResponse());
		SsoLoginLogParam param = new SsoLoginLogParam();
		param.setSsoLoginLog(loginLogDto);
		if (StringUtils.isNotBlank(loginResult.getRequest())) {
			logger.info("SAML跳转成功");
		} else {
			String msg = loginLogDto.getIsError() == 0 ? "成功" : "失败";
			logger.info("{}公司{}单点登陆{}", loginConfig.getCompanyCode(), loginConfig.getType(), msg);
		}
		loginLogTargetService.saveSsoLoginLog(param);
	}
}

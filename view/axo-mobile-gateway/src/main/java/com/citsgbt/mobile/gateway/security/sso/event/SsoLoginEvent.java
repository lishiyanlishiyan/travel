package com.citsgbt.mobile.gateway.security.sso.event;

import com.citsgbt.mobile.core.utils.spring.ApplicationContextUtils;
import com.citsgbt.mobile.gateway.security.sso.SsoLoginResultVo;
import com.citsgbt.mobile.gateway.security.sso.exception.SsoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

public class SsoLoginEvent extends ApplicationEvent {

	private static final Logger logger = LoggerFactory.getLogger(SsoLoginEvent.class);

	SsoLoginEvent(SsoLoginResultVo source) {
		super(source);
	}

	public SsoLoginResultVo getSsoLoginResult() {
		return (SsoLoginResultVo) getSource();
	}

	public static SsoLoginSuccessEvent success(SsoLoginResultVo source) {
		return new SsoLoginSuccessEvent(source);
	}

	public static SsoLoginFailureEvent error(SsoLoginResultVo source, SsoException e) {
		return new SsoLoginFailureEvent(source, e);
	}

	public void publish() {
		try {
			ApplicationContextUtils.publishEvent(this);
		} catch (Exception e) {
			logger.error("处理SSO登陆错误", e);
		}
	}
}

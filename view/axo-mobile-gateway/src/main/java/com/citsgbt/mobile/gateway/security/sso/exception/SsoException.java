package com.citsgbt.mobile.gateway.security.sso.exception;

import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import org.springframework.security.core.AuthenticationException;

/**
 * sso异常处理
 *
 * @author gary.fu
 */
public class SsoException extends AuthenticationException {

	private final LoginConfig loginConfig;

	public SsoException(String msg, Throwable t, LoginConfig loginConfig) {
		super(msg, t);
		this.loginConfig = loginConfig;
	}

	public SsoException(String msg, LoginConfig loginConfig) {
		super(msg);
		this.loginConfig = loginConfig;
	}

	public LoginConfig getLoginConfig() {
		return loginConfig;
	}

}

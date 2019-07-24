package com.citsgbt.mobile.gateway.security.sso.event;

import com.citsgbt.mobile.gateway.security.sso.SsoLoginResultVo;
import com.citsgbt.mobile.gateway.security.sso.exception.SsoException;

public class SsoLoginFailureEvent extends SsoLoginEvent {

	private SsoException exception;

	public SsoLoginFailureEvent(SsoLoginResultVo source, SsoException exception) {
		super(source);
		this.exception = exception;
	}

	public SsoException getException() {
		return exception;
	}

	public void setException(SsoException exception) {
		this.exception = exception;
	}
}

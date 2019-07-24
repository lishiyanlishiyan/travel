package com.citsgbt.mobile.gateway.security.sso.event;

import com.citsgbt.mobile.gateway.security.sso.SsoLoginResultVo;

public class SsoLoginSuccessEvent extends SsoLoginEvent {

	public SsoLoginSuccessEvent(SsoLoginResultVo source) {
		super(source);
	}
}

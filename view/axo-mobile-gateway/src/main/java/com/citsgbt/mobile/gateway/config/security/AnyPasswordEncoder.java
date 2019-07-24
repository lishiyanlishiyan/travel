package com.citsgbt.mobile.gateway.config.security;


import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created on 2018/12/22 13:21.<br>
 *
 * @author gary.fu
 */
public class AnyPasswordEncoder implements PasswordEncoder {

	@Override
	public String encode(CharSequence rawPass) {
		return rawPass.toString();
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return true;
	}
}

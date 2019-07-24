package com.citsgbt.mobile.core.utils.encoder;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author gary.fu
 * @see org.springframework.security.crypto.password.NoOpPasswordEncoder
 */
public final class NoOpPasswordEncoder implements PasswordEncoder {

	private static final PasswordEncoder INSTANCE = new NoOpPasswordEncoder();

	public String encode(CharSequence rawPassword) {
		return rawPassword.toString();
	}

	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return rawPassword.toString().equals(encodedPassword);
	}

	public static PasswordEncoder getInstance() {
		return INSTANCE;
	}

	private NoOpPasswordEncoder() {
	}
}
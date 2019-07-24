package com.citsgbt.mobile.gateway.config.security;


import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2016/2/14 13:21.<br>
 * 新添加了盐值的认证规则
 *
 * @author gary.fu
 */
class Sha512WithSaltPasswordEncoder implements PasswordEncoder {

	private static final Pattern PWD_PATTERN = Pattern.compile("(.+)\\{\\{(.+)}}");

	@Override
	public String encode(CharSequence rawPass) {
		String rawPassword = StringUtils.isBlank(rawPass) ? StringUtils.EMPTY : rawPass.toString();
		Matcher matcher = PWD_PATTERN.matcher(rawPassword);
		String salt = null;
		if (matcher.matches()) {
			rawPassword = matcher.group(1);
			salt = matcher.group(2);
		}
		if (salt == null) { // old way
			return EncryptionUtils.irreversibleEncrypt(rawPassword, EncryptionUtils.ALGORITHM_SHA_512);
		} else {
			return EncryptionUtils.irreversibleEncryptWithSalt(EncryptionUtils.irreversibleEncrypt(rawPassword, EncryptionUtils.ALGORITHM_SHA_512), salt, EncryptionUtils.ALGORITHM_SHA_512);
		}
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		return StringUtils.equals(encode(rawPassword), encodedPassword);
	}
}

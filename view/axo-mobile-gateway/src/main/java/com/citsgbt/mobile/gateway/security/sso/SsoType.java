package com.citsgbt.mobile.gateway.security.sso;

import org.apache.commons.lang3.StringUtils;

public enum SsoType {

	SAML("saml"), // SAML方式
	CAS("cas"), // CAS方式
	COMMON("common"), // 简单单点登陆，验证Ip地址，通过请求参数或者请求头信息传递公司代码和用户登陆字段,
	JWT("jwt"), // 传递access_token字段
	OAUTH("oauth"), // oauth登陆
	WS("ws"); // 传递一个Token过来，通过WebService验证合法性

	private final String value;

	SsoType(final String ssoType) {
		this.value = ssoType;
	}

	public static SsoType fromValue(String type) {
		if (StringUtils.isNotBlank(type)) {
			for (SsoType ssoType : SsoType.values()) {
				if (StringUtils.equalsIgnoreCase(type, ssoType.value)) {
					return ssoType;
				}
			}
		}
		return null;
	}

	/**
	 * 获取String值
	 *
	 * @return
	 */
	public String getValue() {
		return value;
	}
}

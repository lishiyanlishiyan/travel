package com.citsgbt.mobile.gateway.security.sso.vo;

import com.citsamex.app.spi.data.dto.base.CommonDto;

public class SsoConfigMapping extends CommonDto {

	private String property;

	private String key;

	private String defaultValue;

	private SsoConfigMapping(String property, String key, String defaultValue) {
		this.property = property;
		this.key = key;
		this.defaultValue = defaultValue;
	}

	public static SsoConfigMapping of(String property, String key) {
		return new SsoConfigMapping(property, key, null);
	}

	public static SsoConfigMapping of(String property, String key, String defaultValue) {
		return new SsoConfigMapping(property, key, defaultValue);
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}

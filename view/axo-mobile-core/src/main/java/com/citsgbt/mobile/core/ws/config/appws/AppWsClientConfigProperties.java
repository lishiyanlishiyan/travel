package com.citsgbt.mobile.core.ws.config.appws;

import com.citsgbt.mobile.core.ws.config.ClientConfigProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * @author gary.fu
 */
@ConfigurationProperties(prefix = "ws.client.appws")
public class AppWsClientConfigProperties extends ClientConfigProperties {

	private String partnerId;

	private String format = "json";

	private String encryptPassword;

	private boolean encryptEnabled;

	private Set<ServiceLocateKey> proxyExcludes = new HashSet<>();

	public String getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Set<ServiceLocateKey> getProxyExcludes() {
		return proxyExcludes;
	}

	public void setProxyExcludes(Set<ServiceLocateKey> proxyExcludes) {
		this.proxyExcludes = proxyExcludes;
	}

	public String getEncryptPassword() {
		return encryptPassword;
	}

	public void setEncryptPassword(String encryptPassword) {
		this.encryptPassword = encryptPassword;
	}

	public boolean isEncryptEnabled() {
		return encryptEnabled;
	}

	public void setEncryptEnabled(boolean encryptEnabled) {
		this.encryptEnabled = encryptEnabled;
	}
}

package com.citsgbt.mobile.gateway.config.oauth2;

import com.citsgbt.mobile.core.client.SimpleClient;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "simple.sso")
public class SimpleSsoConfigProperties {

	private String defaultClientId;

	private String defaultRedirectUri;

	private List<SimpleClient> clients = new ArrayList<>();

	private String onlineSsoUrl;

	private String onlineSsoTokenName = SSOUtils.JWT_ACCESS_TOKEN_KEY;

	private boolean onlineSso;

	public List<SimpleClient> getClients() {
		return clients;
	}

	public void setClients(List<SimpleClient> clients) {
		this.clients = clients;
	}

	public String getDefaultRedirectUri() {
		return defaultRedirectUri;
	}

	public void setDefaultRedirectUri(String defaultRedirectUri) {
		this.defaultRedirectUri = defaultRedirectUri;
	}

	public String getDefaultClientId() {
		return defaultClientId;
	}

	public void setDefaultClientId(String defaultClientId) {
		this.defaultClientId = defaultClientId;
	}

	public String getOnlineSsoUrl() {
		return onlineSsoUrl;
	}

	public void setOnlineSsoUrl(String onlineSsoUrl) {
		this.onlineSsoUrl = onlineSsoUrl;
	}

	public boolean isOnlineSso() {
		return onlineSso;
	}

	public void setOnlineSso(boolean onlineSso) {
		this.onlineSso = onlineSso;
	}

	public String getOnlineSsoTokenName() {
		return onlineSsoTokenName;
	}

	public void setOnlineSsoTokenName(String onlineSsoTokenName) {
		this.onlineSsoTokenName = onlineSsoTokenName;
	}
}

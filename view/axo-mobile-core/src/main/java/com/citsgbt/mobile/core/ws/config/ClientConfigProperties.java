package com.citsgbt.mobile.core.ws.config;

public abstract class ClientConfigProperties {

	private String wsAddress;

	private String userName;

	private String userPassword;

	private long connectTimeout = 10 * 1000L;

	private long socketTimeout = 120 * 1000L;

	public String getWsAddress() {
		return wsAddress;
	}

	public void setWsAddress(String wsAddress) {
		this.wsAddress = wsAddress;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public long getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(long connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public long getSocketTimeout() {
		return socketTimeout;
	}

	public void setSocketTimeout(long socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
}

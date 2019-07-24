package com.citsgbt.mobile.core.web.vo;

import java.io.Serializable;

public class SsoParamsDto implements Serializable {

	private static final long serialVersionUID = 634218372389912223L;

	private String externalOrderNo;

	private String taNo;

	private String gotoPath;

	public String getExternalOrderNo() {
		return externalOrderNo;
	}

	public void setExternalOrderNo(String externalOrderNo) {
		this.externalOrderNo = externalOrderNo;
	}

	public String getTaNo() {
		return taNo;
	}

	public void setTaNo(String taNo) {
		this.taNo = taNo;
	}

	public String getGotoPath() {
		return gotoPath;
	}

	public void setGotoPath(String gotoPath) {
		this.gotoPath = gotoPath;
	}
}

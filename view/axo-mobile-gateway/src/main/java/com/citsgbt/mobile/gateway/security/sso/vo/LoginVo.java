package com.citsgbt.mobile.gateway.security.sso.vo;

import com.citsamex.app.spi.data.dto.base.CommonDto;
import org.apache.commons.lang3.StringUtils;

public class LoginVo extends CommonDto {

	private String companyCode;

	private String loginName;

	public LoginVo() {
	}

	public LoginVo(String companyCode) {
		this.companyCode = StringUtils.trimToEmpty(companyCode);
	}

	public LoginVo(String companyCode, String loginName) {
		this(companyCode);
		this.loginName = StringUtils.trimToEmpty(loginName);
	}

	public String toUsername() {
		return StringUtils.join(getCompanyCode(), ",", getLoginName());
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
}

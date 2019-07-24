package com.citsgbt.mobile.gateway.web.vo;

import com.citsamex.app.spi.data.dto.company.SsoConfigDto;
import com.citsgbt.mobile.core.utils.lang.DozerUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.OauthConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;

import java.io.Serializable;

public class SsoConfigVo implements Serializable {

	private static final long serialVersionUID = 7373241064062858764L;
	private String companyCode;
	private Integer normalLoginFlag;
	private String key;
	private String type;
	private String url;

	public SsoConfigVo() {
	}

	public SsoConfigVo(SsoConfigDto ssoConfig, LoginConfig loginConfig) {
		DozerUtils.map(ssoConfig, this);
		if (loginConfig instanceof SAMLConfig) {
			setUrl(((SAMLConfig) loginConfig).getAssertionConsumerServiceURL());
		} else if (loginConfig instanceof OauthConfig) {
			setUrl(((OauthConfig) loginConfig).getOauthRedirectUri());
		}
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public Integer getNormalLoginFlag() {
		return normalLoginFlag;
	}

	public void setNormalLoginFlag(Integer normalLoginFlag) {
		this.normalLoginFlag = normalLoginFlag;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}

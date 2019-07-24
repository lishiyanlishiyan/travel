package com.citsgbt.mobile.gateway.web.vo;

import com.citsamex.app.spi.data.dto.base.CommonDto;
import org.apache.commons.lang3.StringUtils;

/**
 * @author gary.fu
 */
public class OnlineSsoVo extends CommonDto {

	private boolean enabled;

	private String url;

	public OnlineSsoVo() {
	}

	public OnlineSsoVo(String url) {
		this.url = StringUtils.trimToEmpty(url);
		this.enabled = StringUtils.isNotBlank(url);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}

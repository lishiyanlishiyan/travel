package com.citsgbt.mobile.core.ws.config.appws;

import com.citsamex.app.spi.data.dto.base.CommonDto;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author gary.fu
 */
public class ServiceLocateKey extends CommonDto {

	private String path;

	private String name;

	public ServiceLocateKey() {
	}

	public ServiceLocateKey(String path, String name) {
		this.name = name;
		this.path = path;
	}

	public static ServiceLocateKey of(String path, String name) {
		return new ServiceLocateKey(path, name);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}
}

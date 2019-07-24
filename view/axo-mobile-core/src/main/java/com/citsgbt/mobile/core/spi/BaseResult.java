package com.citsgbt.mobile.core.spi;

import com.citsamex.app.spi.data.dto.base.CommonDto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created on 2016/3/18 16:43.<br>
 *
 * @author gary.fu
 */
@SuppressWarnings("serial")
@XmlRootElement
class BaseResult extends CommonDto {

	private String code;

	private String msg;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public <T extends BaseResult> T success() {
		return code(SPIConsts.SUCCESS);
	}

	public <T extends BaseResult> T error() {
		return code(SPIConsts.ERROR);
	}

	public <T extends BaseResult> T error(String msg) {
		return code(SPIConsts.ERROR).msg(msg);
	}

	public <T extends BaseResult> T errorCode(String code) {
		return code(code);
	}

	public <T extends BaseResult> T fallback() {
		return code(SPIConsts.FALLBACK);
	}

	public <T extends BaseResult> T invalid() {
		return code(SPIConsts.NOT_AVAILABLE);
	}

	public <T extends BaseResult> T code(String code) {
		this.code = code;
		return (T) this;
	}

	public <T extends BaseResult> T msg(String msg) {
		this.msg = msg;
		return (T) this;
	}

	public boolean isSuccess() {
		return SPIConsts.SUCCESS.equals(code);
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}

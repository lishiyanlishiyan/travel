package com.citsgbt.mobile.core.web.vo;

import com.citsamex.app.spi.data.dto.base.CommonDto;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created on 2014/8/29 15:01.<br>
 *
 * @author gary.fu
 */
public class ErrorInfo extends CommonDto {

	private String errorCode;

	private String error;

	private String errorDetails;

	private Throwable exception;

	private String linkUrl;

	public ErrorInfo() {
	}

	public ErrorInfo(String errorCode) {
		this.errorCode = errorCode;
	}

	public ErrorInfo(String errorCode, Throwable exception) {
		this.errorCode = errorCode;
		this.exception = exception;
	}

	public ErrorInfo(String errorCode, String error, String errorDetails) {
		this.errorCode = errorCode;
		this.error = error;
		this.errorDetails = errorDetails;
	}

	public ErrorInfo(String errorCode, String error) {
		this.errorCode = errorCode;
		this.error = error;
	}

	public ErrorInfo(String error, String errorDetails, Throwable exception) {
		this.error = error;
		this.errorDetails = errorDetails;
		this.exception = exception;
	}

	public ErrorInfo(String errorCode, String error, String errorDetails, Throwable exception) {
		this.errorCode = errorCode;
		this.error = error;
		this.errorDetails = errorDetails;
		this.exception = exception;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getErrorDetails() {
		return errorDetails;
	}

	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

	public String getLinkUrl() {
		return linkUrl;
	}

	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}

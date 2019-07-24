/**
 *
 */
package com.citsgbt.mobile.core.web.result;

import com.citsamex.app.spi.data.base.CallerResponse;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用json方式输出的时候包装成此对象
 *
 * @author gary.fu
 *
 */
public class JsonResultMessage<T> implements Serializable {
	/**
	 * 是否成功
	 */
	private boolean success;

	/**
	 * 数据
	 */
	private T resultData;

	/**
	 * 错误消息
	 */
	private String message;

	/**
	 * Session超时标识
	 */
	private boolean sessionTimeout = false;

	/**
	 * Session超时标识
	 */
	private boolean fatalError = false;

	/**
	 * 其他附加信息
	 */
	private Map<String, Object> infos = new HashMap<>();

	/**
	 * 跳转url
	 */
	private String backUrl;

	/**
	 *
	 * @param messages
	 * @return
	 */
	public static JsonResultMessage responseSuccess(String... messages) {
		return response(true, messages);
	}

	/**
	 *
	 * @param messages
	 * @return
	 */
	public static JsonResultMessage responseError(String... messages) {
		return response(false, messages);
	}

	/**
	 *
	 * @param messages
	 * @return
	 */
	public static <T> JsonResultMessage<T> responseSuccessData(T data, String... messages) {
		return responseWithData(data, true, messages);
	}

	/**
	 *
	 * @param messages
	 * @return
	 */
	public static <T> JsonResultMessage<T> responseErrorData(T data, String... messages) {
		return responseWithData(data, false, messages);
	}

	/**
	 *
	 * @param messages
	 * @return
	 */
	private static JsonResultMessage response(boolean success, String... messages) {
		return new JsonResultMessage().putSuccess(success).putMessage(messages);
	}

	/**
	 *
	 * @param messages
	 * @return
	 */
	private static <T> JsonResultMessage<T> responseWithData(T data, boolean success, String... messages) {
		return new JsonResultMessage().putData(data).putSuccess(success).putMessage(messages);
	}

	/**
	 *
	 * @param response
	 * @param <T>
	 * @return
	 */
	public static <T extends CallerResponse> JsonResultMessage<T> fromCallerResponse(T response) {
		if (response.success()) {
			return JsonResultMessage.responseSuccessData(response);
		} else {
			return JsonResultMessage.responseErrorData(response, response.getResponseHead().getResultMessage());
		}
	}

	/**
	 * @param success
	 * @return
	 */
	private JsonResultMessage putSuccess(boolean success) {
		setSuccess(success);
		return this;
	}

	/**
	 * @param messages
	 * @return
	 */
	private JsonResultMessage putMessage(String... messages) {
		setMessage(StringUtils.join(messages));
		return this;
	}

	/**
	 * @param data
	 * @return
	 */
	private JsonResultMessage putData(T data) {
		setResultData(data);
		return this;
	}

	/**
	 * @param backUrl
	 * @return
	 */
	public JsonResultMessage putBackUrl(String backUrl) {
		setBackUrl(backUrl);
		return this;
	}

	/**
	 * @param resultMessage
	 * @return
	 */
	public JsonResultMessage parseBackUrl(JsonResultMessage resultMessage) {
		if (resultMessage != null && StringUtils.isNotBlank(resultMessage.getBackUrl())) {
			this.setBackUrl(resultMessage.getBackUrl());
		}
		return this;
	}

	/**
	 * @param infos
	 * @return
	 */
	public JsonResultMessage putInfos(Map<String, Object> infos) {
		setInfos(infos);
		return this;
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 */
	public JsonResultMessage addInfo(String key, Object value) {
		getInfos().put(key, value);
		return this;
	}

	/**
	 * @return
	 */
	public JsonResultMessage putSessionTimeout() {
		setSessionTimeout(true);
		return this;
	}

	/**
	 * @return
	 */
	public JsonResultMessage putFatalError() {
		setFatalError(true);
		return this;
	}

	// *************************getter and setter**************//

	/**
	 * @return the sessionTimeout
	 */
	public boolean isSessionTimeout() {
		return sessionTimeout;
	}

	/**
	 * @param sessionTimeout
	 *            the sessionTimeout to set
	 */
	private void setSessionTimeout(boolean sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	/**
	 * @return the success
	 */
	public boolean getSuccess() {
		return success;
	}

	/**
	 * @param success
	 *            the success to set
	 */
	private void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * @return the messages
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the messages to set
	 */
	private void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the infos
	 */
	private Map<String, Object> getInfos() {
		return infos;
	}

	/**
	 * @param infos
	 *            the infos to set
	 */
	private void setInfos(Map<String, Object> infos) {
		this.infos = infos;
	}

	/**
	 * @return the backUrl
	 */
	private String getBackUrl() {
		return backUrl;
	}

	/**
	 * @param backUrl
	 *            the backUrl to set
	 */
	void setBackUrl(String backUrl) {
		this.backUrl = backUrl;
	}

	public T getResultData() {
		return resultData;
	}

	private void setResultData(T resultData) {
		this.resultData = resultData;
	}

	public boolean isFatalError() {
		return fatalError;
	}

	private void setFatalError(boolean fatalError) {
		this.fatalError = fatalError;
	}
}

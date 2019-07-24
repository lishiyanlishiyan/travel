package com.citsgbt.mobile.core.ws.config.appws;

import com.citsamex.app.spi.data.base.AbstractServiceParam;
import com.citsamex.app.spi.data.base.AccountPasswordToken;
import com.citsamex.app.spi.data.base.CallerRequest;
import com.citsamex.app.spi.data.base.CallerResponse;
import com.citsamex.app.spi.interfaces.caller.ServiceCaller;
import com.citsamex.app.spi.utils.security.SecurityTokenUtils;
import com.citsgbt.connect.encrypt.appws.AppDataEncryptProvider;
import com.citsgbt.connect.utils.appws.AppDataEncryptUtils;
import com.citsgbt.mobile.core.utils.callback.Callback;
import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.core.web.result.JsonResultMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Created on 2014/8/6 14:36.<br>
 *
 * @author gary.fu
 */
public class ServiceCallerUtils {

	private static final Logger logger = LoggerFactory.getLogger(ServiceCallerUtils.class);

	private static ServiceCaller serviceCaller;

	private static AppWsClientConfigProperties configProperties;

	private static AppDataEncryptProvider appDataEncryptProvider;

	private static final ThreadLocal<String> LOGIN_USER_ID = new ThreadLocal<>();

	private static final ThreadLocal<String> LOGIN_LOCALE = new ThreadLocal<>();

	@Autowired(required = false)
	public void inject(ServiceCaller caller, AppWsClientConfigProperties appWsClientConfigProperties, AppDataEncryptProvider encryptProvider) {
		setServiceCaller(caller);
		setConfigProperties(appWsClientConfigProperties);
		setAppDataEncryptProvider(encryptProvider);
	}

	private static ServiceCaller getServiceCaller() {
		Validate.notNull(serviceCaller, "APP服务没有初始化");
		return serviceCaller;
	}

	/**
	 * 调用
	 *
	 * @param callerRequest
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	@SuppressWarnings("squid:S1172")
	private static <T> T invokeProxy(CallerRequest callerRequest, Class<T> clazz) {
		if (callerRequest.getRequestHead().getAccountPasswordToken() == null) {
			AccountPasswordToken token = new AccountPasswordToken();
			token.setAccountId(configProperties.getUserName());
			token.setPartnerId(configProperties.getPartnerId());
			String created = SecurityTokenUtils.getCreated(new Date());
			token.setCreated(created);
			try {
				String nonce = SecurityTokenUtils.generateNonceStr(16);
				token.setNonce(nonce);
				token.setPassword(SecurityTokenUtils.doPasswordDigest(nonce, created, configProperties.getUserPassword()));
			} catch (Exception e) {
				logger.error("nonce or password generate error", e);
			}
			callerRequest.getRequestHead().setAccountPasswordToken(token);
		}
		callerRequest.getRequestHead().setClientIp(HttpRequestUtils.getClientIp());
		appDataEncryptProvider.encrypt(callerRequest, AppDataEncryptUtils.DEFAULT_KEY);
		CallerResponse response = getServiceCaller().callService(callerRequest);
		appDataEncryptProvider.decrypt(response, AppDataEncryptUtils.DEFAULT_KEY);
		if (response != null && !response.success()) {
			logger.error("执行服务返回错误【{}/{}】", response.getResponseHead().getResultCode(), response.getResponseHead().getResultMessage());
		}
		return (T) response;
	}

	/**
	 * 调用包装
	 *
	 * @param callerRequest
	 * @param userId
	 * @param locale
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	private static <T> T invokeProxy(CallerRequest callerRequest, String userId, String locale, Class<T> clazz) {
		callerRequest.getRequestHead().setUserId(userId);
		callerRequest.getRequestHead().setLocale(locale);
		return invokeProxy(callerRequest, clazz);
	}

	/**
	 * 调用包装
	 *
	 * @param callerRequest
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public static <T> T invokeDefault(CallerRequest callerRequest, Class<T> clazz) {
		String userId = LOGIN_USER_ID.get();
		if (StringUtils.isBlank(userId) && StringUtils.isNotBlank(callerRequest.getRequestHead().getUserId())) {
			userId = callerRequest.getRequestHead().getUserId();
		}
		HttpServletRequest httpRequest = HttpRequestUtils.getCurrentRequest();
		if (httpRequest != null) {
			if (StringUtils.isBlank(userId)) {
				userId = httpRequest.getRemoteUser();
			}
			if (StringUtils.isBlank(userId)) {
				userId = httpRequest.getHeader("axo_login_id");
			}
		}
		String locale = LOGIN_LOCALE.get();
		if (StringUtils.isBlank(locale) && StringUtils.isNotBlank(callerRequest.getRequestHead().getLocale())) {
			locale = callerRequest.getRequestHead().getLocale();
		}
		if (StringUtils.isBlank(locale)) {
			locale = HttpRequestUtils.getCurrentLocale();
		}
		return invokeProxy(callerRequest, userId, locale, clazz);
	}

	/**
	 * 调用webservice接口
	 *
	 * @param servicePath
	 * @param serviceName
	 * @param param
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public static <T> T invokeDefault(String servicePath, String serviceName, AbstractServiceParam param, Class<T> clazz) {
		CallerRequest callerRequest = new CallerRequest();
		callerRequest.setServicePath(servicePath);
		callerRequest.setServiceName(serviceName);
		callerRequest.setServiceParamObj(param);
		return invokeDefault(callerRequest, clazz);
	}

	public static void doWithUserId(String userId, Callback<String> callback) {
		doWithUserIdAndLocale(userId, null, callback);
	}

	private static void doWithUserIdAndLocale(String userId, String locale, Callback<String> callback) {
		LOGIN_USER_ID.set(userId);
		if (StringUtils.isNotBlank(locale)) {
			LOGIN_LOCALE.set(locale);
		}
		try {
			if (callback != null) {
				callback.doCallback(userId);
			}
		} catch (Exception e) {
			logger.error("执行回调错误", e);
		} finally {
			LOGIN_USER_ID.remove();
			if (StringUtils.isNotBlank(locale)) {
				LOGIN_LOCALE.remove();
			}
		}
	}

	/**
	 * 解析Response为JsonResultMessage对象
	 *
	 * @param response
	 * @param <T>
	 * @return
	 */
	public static <T extends CallerResponse> JsonResultMessage<T> parseResponse(T response) {
		if (response == null) {
			return JsonResultMessage.responseErrorData(response, "app.return.null");
		} else if (!response.success()) {
			return JsonResultMessage.responseErrorData(response, response.getResponseHead().getResultCode());
		} else {
			return JsonResultMessage.responseSuccessData(response);
		}
	}

	public static void setServiceCaller(ServiceCaller serviceCaller) {
		ServiceCallerUtils.serviceCaller = serviceCaller;
	}

	public static AppWsClientConfigProperties getConfigProperties() {
		return configProperties;
	}

	public static void setConfigProperties(AppWsClientConfigProperties configProperties) {
		ServiceCallerUtils.configProperties = configProperties;
	}

	public static AppDataEncryptProvider getAppDataEncryptProvider() {
		return appDataEncryptProvider;
	}

	public static void setAppDataEncryptProvider(AppDataEncryptProvider appDataEncryptProvider) {
		ServiceCallerUtils.appDataEncryptProvider = appDataEncryptProvider;
	}
}

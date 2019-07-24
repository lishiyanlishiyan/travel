/**
 *
 */
package com.citsgbt.mobile.core.utils.http;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author gary.fu
 */
public class HttpRequestUtils {

	private static final Logger logger = LoggerFactory.getLogger(HttpRequestUtils.class);

	private static final String IP_UNKNOW = "unknown";

	private HttpRequestUtils() {

	}

	/**
	 * 取得访问IP
	 *
	 * @param request
	 * @return
	 */
	public static String getIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (StringUtils.isBlank(ip) || IP_UNKNOW.equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (StringUtils.isBlank(ip) || IP_UNKNOW.equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (StringUtils.isBlank(ip) || IP_UNKNOW.equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Real-IP");
		}
		if (StringUtils.isBlank(ip) || IP_UNKNOW.equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	public static String getRequestUrl(HttpServletRequest request){
		StringBuffer url = request.getRequestURL();
		String queryString = request.getQueryString();
		if (StringUtils.isNotBlank(queryString)) {
			url.append("?").append(queryString);
		}
		return url.toString();
	}

	/**
	 * 判断Ajax输出json
	 *
	 * @param request
	 * @return
	 */
	public static boolean isAjax(HttpServletRequest request) {
		String accept = request.getHeader("Accept");
		return request.getHeader("X-Requested-With") != null
				|| request.getHeader("is-ajax") != null
				|| StringUtils.indexOf(accept, "application/json") != -1;
	}

	/**
	 * 解析区域国际化
	 *
	 * @param request
	 * @return
	 */
	public static String getParsedLocale(HttpServletRequest request) {
		return getLocale(request).toString();
	}

	/**
	 * @param request
	 * @return
	 */
	public static Locale getLocale(HttpServletRequest request) {
		if (request != null) {
			Locale locale = RequestContextUtils.getLocale(request);
			if (Locale.CHINA.equals(locale)
					|| Locale.CHINESE.equals(locale)) {
				return Locale.CHINA;
			}
		}
		return Locale.US;
	}

	private static HttpSession getCurrentSession() {
		HttpServletRequest currentRequest = getCurrentRequest();
		if (currentRequest != null) {
			return currentRequest.getSession();
		}
		return null;
	}

	public static HttpServletRequest getCurrentRequest() {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return requestAttributes != null ? requestAttributes.getRequest() : null;
	}

	public static HttpServletResponse getCurrentResponse() {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return requestAttributes != null ? requestAttributes.getResponse() : null;
	}

	public static String getCurrentLocale() {
		HttpServletRequest currentRequest = getCurrentRequest();
		if (currentRequest != null) {
			String language = currentRequest.getHeader("language");
			if(StringUtils.isBlank(language)){
				language = currentRequest.getHeader("axo_locale");
			}
			if(StringUtils.isNotBlank(language)){
				return language;
			}
			return getLocale(currentRequest).toString();
		}
		return "zh_CN";
	}

	public static String getRequestPath(HttpServletRequest request) {
		String url = request.getServletPath();
		if (request.getPathInfo() != null) {
			url += request.getPathInfo();
		}
		return url;
	}

	/**
	 * 获取搜索参数
	 *
	 * @return
	 */
	public static Map<String, Object> getSearchParams(String spId) {
		HttpSession session = getCurrentSession();
		Map<String, Object> ccMap = new HashMap<>();
		if (session != null && StringUtils.isNotBlank(spId)) {
			ccMap = (Map<String, Object>) session.getAttribute(spId);
			session.removeAttribute(spId);
		}
		if (ccMap == null) {
			ccMap = new HashMap<>();
		}
		return ccMap;
	}

	public static String getClientIp() {
		HttpServletRequest currentRequest = getCurrentRequest();
		String ipAddr = null;
		if (currentRequest != null) {
			ipAddr = getIp(currentRequest);
			try {
				if (StringUtils.isNotBlank(ipAddr) && InetAddress.getByName(ipAddr).isLoopbackAddress()) {
					ipAddr = InetAddress.getLocalHost().getHostAddress();
				}
			} catch (Exception e) {
				logger.error("获取IP错误", e);
			}
		}
		return ipAddr;
	}

	/**
	 * 处理请求参数
	 */
	public static Map<String, Object> processInputMap(HttpServletRequest request) {
		Map<String, String> parameters = new HashMap<>();
		Map<String, String> headers = new HashMap<>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String name = headerNames.nextElement();
			headers.put(name, request.getHeader(name));
		}
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String name = parameterNames.nextElement();
			parameters.put(name, request.getParameter(name));
		}
		Map<String, Object> inputMap = new HashMap<>();
		inputMap.put("Parameters", parameters);
		inputMap.put("Headers", headers);
		inputMap.put("RequestMethod", request.getMethod());
		inputMap.put("Url", HttpRequestUtils.getRequestUrl(request));
		return inputMap;
	}

}

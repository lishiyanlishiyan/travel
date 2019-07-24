package com.citsgbt.mobile.gateway.security.sso.cas.utils;

import com.citsamex.app.spi.utils.json.JsonUtils;
import com.citsgbt.mobile.core.utils.spring.MessageSourceUtils;
import com.citsgbt.mobile.gateway.security.sso.exception.SsoException;
import com.citsgbt.mobile.gateway.security.sso.vo.CASConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CasParseUtils {

	private CasParseUtils() {

	}

	/**
	 * Json格式name
	 *
	 * @param name
	 * @return
	 */
	private static Map<String, Object> parseIfJson(String name) {
		name = StringUtils.trimToEmpty(name);
		Map<String, Object> jsonMap = new HashMap<>();
		if (StringUtils.startsWith(name, "{") && StringUtils.endsWith(name, "}")) { // json格式
			jsonMap = JsonUtils.fromJson(name, Map.class);
		} else {
			jsonMap.put("name", name);
		}
		return jsonMap;
	}

	/**
	 * 解析返回的数据
	 *
	 * @param assertion
	 * @param casConfig
	 * @return
	 */
	public static LoginVo parseAssertion(Assertion assertion, CASConfig casConfig) {
		String loginName = StringUtils.EMPTY;
		if (assertion != null) {
			Date validUtil = assertion.getValidUntilDate();
			if (validUtil != null && validUtil.compareTo(new Date()) < 0) {
				throw new SsoException(MessageSourceUtils.getMessage("sso.invalid.assert.expired"), casConfig);
			} else {
				loginName = calcLoginName(assertion, casConfig);
			}
		}
		return new LoginVo(casConfig.getCompanyCode(), loginName);
	}

	public static String calcLoginName(Assertion assertion, CASConfig casConfig) {
		String loginName;
		AttributePrincipal principal = assertion.getPrincipal();
		if (principal != null) {
			String loginField = StringUtils.trimToEmpty(StringUtils.isBlank(casConfig.getCasLoginField()) ? "name" : casConfig.getCasLoginField());
			Map<String, Object> assertAttributes = principal.getAttributes() == null ? new HashMap<>() : principal.getAttributes();
			Map<String, Object> nameMap = parseIfJson(principal.getName());
			loginName = (String) nameMap.get(loginField);
			if (StringUtils.isBlank(loginName)) {
				loginName = (String) assertAttributes.get(loginField);
			}
		} else {
			throw new SsoException(MessageSourceUtils.getMessage("sso.invalid.no.principal"), casConfig);
		}
		return loginName;
	}
}

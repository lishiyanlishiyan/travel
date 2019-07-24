package com.citsgbt.mobile.gateway.security.sso.cas;

import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.gateway.security.sso.SsoLoginResultVo;
import com.citsgbt.mobile.gateway.security.sso.cas.utils.CasParseUtils;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.CASConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.security.cas.ServiceProperties;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.citsgbt.mobile.gateway.security.sso.utils.Keys.SSO_LOGIN_RESULT_KEY;

/**
 * @author gary.fu
 */
public class CustomProxyTicketValidator implements TicketValidator {

	public static final String CAS_SERVICE_RESPONSE_KEY = "CAS_SERVICE_RESPONSE_KEY";

	private final TicketValidator ticketValidator;

	public CustomProxyTicketValidator(TicketValidator ticketValidator) {
		this.ticketValidator = ticketValidator;
	}

	@Override
	public Assertion validate(String ticket, String service) throws TicketValidationException {
		HttpServletRequest request = HttpRequestUtils.getCurrentRequest();
		Assertion assertion = null;
		try {
			assertion = this.ticketValidator.validate(ticket, service);
		} finally {
			String response = (String) request.getAttribute(CAS_SERVICE_RESPONSE_KEY);
			Map<String, Object> resultMap = HttpRequestUtils.processInputMap(request);
			resultMap.put(ServiceProperties.DEFAULT_CAS_ARTIFACT_PARAMETER, ticket);
			resultMap.put(ServiceProperties.DEFAULT_CAS_SERVICE_PARAMETER, service);
			resultMap.put("validateResponse", response);
			LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
			String ip = HttpRequestUtils.getIp(request);
			LoginVo loginVo = CasParseUtils.parseAssertion(assertion, (CASConfig) loginConfig);
			SsoLoginResultVo resultVo = new SsoLoginResultVo(loginConfig, resultMap, loginVo);
			resultVo.setIpAddress(ip);
			request.setAttribute(SSO_LOGIN_RESULT_KEY, resultVo);
		}
		return assertion;
	}
}

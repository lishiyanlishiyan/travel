package com.citsgbt.mobile.open.web.controllers;

import com.citsamex.app.spi.data.caller.common.BaseCompanyUserParam;
import com.citsamex.app.spi.data.caller.request.profile.LoginParam;
import com.citsamex.app.spi.data.caller.response.profile.GetUserBasicInfoResponse;
import com.citsamex.app.spi.data.caller.response.profile.LoginResponse;
import com.citsamex.app.spi.interfaces.targets.profile.LoginTargetService;
import com.citsamex.app.spi.interfaces.targets.profile.ProfileTargetService;
import com.citsgbt.mobile.core.spi.AxoLoginConsts;
import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.core.web.result.JsonResultMessage;
import com.citsgbt.mobile.core.web.vo.SsoParamsDto;
import com.citsgbt.mobile.core.ws.config.sso.SsoParamConfigProperties;
import com.citsgbt.mobile.open.service.login.InitLoginService;
import com.citsgbt.mobile.open.web.vo.LoginResultVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;

@Controller
public class LoginController {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private InitLoginService initLoginService;

	@Autowired
	private LoginTargetService loginTargetService;

	@Autowired
	private ProfileTargetService profileTargetService;

	@Autowired
	private OAuth2RestTemplate oauth2RestTemplate;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private SsoParamConfigProperties ssoParamConfigProperties;

	/**
	 * login
	 *
	 * @return JsonResultMessage
	 */
	@ResponseBody
	@PostMapping(value = "/login")
	public JsonResultMessage login(@RequestBody LoginParam param) {
		param.setInternalFlag(true);
		LoginResponse response = loginTargetService.login(param, null);
		String errorMessage = null;
		if (response != null && response.success() && response.getUserBasic() != null) {
			ResourceOwnerPasswordResourceDetails auth2ProtectedResourceDetails = (ResourceOwnerPasswordResourceDetails) oauth2RestTemplate.getResource();
			auth2ProtectedResourceDetails.setUsername(StringUtils.join(param.getCompanyCode(), ",", param.getLoginName()));
			auth2ProtectedResourceDetails.setPassword(param.getPassword());
			OAuth2AccessToken accessToken = null;
			try {
				accessToken = oauth2RestTemplate.getAccessToken();
			} catch (Exception e) {
				logger.error("Token获取错误", e);
				errorMessage = e.getMessage();
			}
			LoginResultVo result = new LoginResultVo();
			result.setLoginResponse(response);
			if (accessToken != null) {
				result.setAccessToken(accessToken.getValue());
				initLoginService.initLogin(result);
				return JsonResultMessage.responseSuccessData(result);
			}
		} else if (response != null && response.getResponseHead() != null) {
			errorMessage = response.getResponseHead().getResultMessage();
		}
		return JsonResultMessage.responseError(errorMessage);
	}

	@ResponseBody
	@PostMapping(value = "/loginByToken")
	public JsonResultMessage loginByToken(HttpServletRequest request) {
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			logger.info("{}:{}", headerName, request.getHeader(headerName));
		}
		String loginName = request.getHeader(AxoLoginConsts.AXO_LOGIN_NAME);
		String accessToken = request.getHeader(AxoLoginConsts.AXO_LOGIN_TOKEN);
		String errorMessage = "sso.login.error";
		if (StringUtils.isNotBlank(loginName) && StringUtils.isNotBlank(accessToken)) {
			String[] loginNames = loginName.split("\\s*,\\s*");
			if (loginNames.length == 2) {
				LoginParam loginParam = new LoginParam();
				loginParam.setCompanyCode(loginNames[0]);
				loginParam.setLoginName(loginNames[1]);
				loginParam.setInternalFlag(true);
				LoginResponse response = loginTargetService.simulateLogin(loginParam, null);
				if (response != null && response.success() && response.getUserBasic() != null) {
					LoginResultVo result = new LoginResultVo();
					result.setLoginResponse(response);
					result.setAccessToken(accessToken);
					initLoginService.initLogin(result);
					loadSsoParams(result);
					return JsonResultMessage.responseSuccessData(result);
				} else {
					if (response != null) {
						errorMessage = response.getResponseHead().getResultMessage();
					}
				}
			}
		}
		return JsonResultMessage.responseError(errorMessage);
	}

	private void loadSsoParams(LoginResultVo result) {
		try {
			String accessToken = result.getAccessToken();
			String key = StringUtils.join(ssoParamConfigProperties.getSsoParamPrefix(), accessToken);
			SsoParamsDto ssoParams = (SsoParamsDto) redisTemplate.opsForValue().get(key);
			result.setSsoParams(ssoParams);
		} catch (Exception e) {
			logger.error("从Redis获取SsoParams错误", e);
		}
	}

	@ResponseBody
	@GetMapping(value = "/loadDefaultSetup")
	public JsonResultMessage<LoginResultVo> loadDefaultSetup(HttpServletRequest request) {
		String userId = request.getHeader(AxoLoginConsts.AXO_LOGIN_ID);
		String companyCode = request.getHeader(AxoLoginConsts.AXO_LOGIN_COMPANY_CODE);
		if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(companyCode)) {
			LoginResultVo result = new LoginResultVo();
			BaseCompanyUserParam param = new BaseCompanyUserParam();
			param.setUserId(userId);
			param.setCompanyCode(companyCode);
			GetUserBasicInfoResponse userResponse = profileTargetService.getUserBasicInfo(param);
			if (userResponse.success() && userResponse.getUserBasic() != null) {
				LoginResponse loginResponse = new LoginResponse();
				loginResponse.setUserBasic(userResponse.getUserBasic());
				result.setLoginResponse(loginResponse);
			}
			initLoginService.reCalcLoginResult(result, companyCode, userId);
			return JsonResultMessage.responseSuccessData(result);
		}
		return JsonResultMessage.responseError("default setup load error");
	}

	@GetMapping(value = "/redirect")
	public String redirect(@RequestParam Map<String, String> params) {
		logger.info("{}", params);
		String code = params.get("code");
		if (StringUtils.isNotBlank(code)) {
			logger.debug(code);
		}
		return null;
	}

	@GetMapping(value = "/getIp")
	@ResponseBody
	public JsonResultMessage getIp() {
		String clientIp = HttpRequestUtils.getClientIp();
		logger.info("{}", clientIp);
		return JsonResultMessage.responseSuccessData(clientIp);
	}
}

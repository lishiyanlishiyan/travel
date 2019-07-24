package com.citsgbt.mobile.gateway.web.controller;

import com.citsamex.app.spi.data.caller.response.company.CompanySsoConfigResponse;
import com.citsamex.app.spi.data.dto.company.SsoConfigDto;
import com.citsgbt.mobile.core.web.result.JsonResultMessage;
import com.citsgbt.mobile.core.web.vo.ErrorInfo;
import com.citsgbt.mobile.gateway.config.oauth2.SimpleSsoConfigProperties;
import com.citsgbt.mobile.gateway.config.security.EncryptionUtils;
import com.citsgbt.mobile.gateway.security.sso.CompanySsoProvider;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.user.CustomUserDetails;
import com.citsgbt.mobile.gateway.web.vo.OnlineSsoVo;
import com.citsgbt.mobile.gateway.web.vo.SsoConfigVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApiIgnore
@Controller
@RequestMapping("/")
public class LoginController {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private CompanySsoProvider companySsoProvider;

	@Autowired
	private SimpleSsoConfigProperties simpleSsoConfigProperties;

	@GetMapping(value = "/login")
	public String login(HttpServletRequest request, Model model) {
		// 登录失败错误
		Exception lastException = (Exception) request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		ErrorInfo errorInfo;
		if (lastException != null) {
			String message = lastException.getMessage();
			if (lastException instanceof InternalAuthenticationServiceException
					|| (lastException instanceof UsernameNotFoundException && StringUtils.equals("ACCOUNT_LOAD_ERROR", lastException.getMessage()))) {
				message = "服务连接失败";
			} else if (lastException instanceof UsernameNotFoundException) {
				// 处理从APP接口返回的错误信息
				message = lastException.getMessage();
			}
			errorInfo = new ErrorInfo(message, ExceptionUtils.getStackTrace(lastException), lastException);
			model.addAttribute("errorInfo", errorInfo);
			request.getSession().removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
			logger.info("ErrorInfo[{}]", errorInfo);
		}
		return "login";
	}

	@ResponseBody
	@PostMapping(value = "/ssoLoginConfig/{companyCode}")
	public JsonResultMessage<List<SsoConfigVo>> ssoLoginConfig(@PathVariable("companyCode") String companyCode) {
		List<SsoConfigDto> ssoConfigList = new ArrayList<>();
		if (StringUtils.isNotBlank(companyCode)) {
			CompanySsoConfigResponse ssoConfigResponse = companySsoProvider.getSsoConfigResponse(companyCode);
			if (ssoConfigResponse != null && ssoConfigResponse.success() && CollectionUtils.isNotEmpty(ssoConfigResponse.getSsoConfigList())) {
				ssoConfigList = ssoConfigResponse.getSsoConfigList();
			}
		}
		List<SsoConfigVo> results = ssoConfigList.stream().map(ssoConfigDto -> {
			LoginConfig loginConfig = SSOUtils.getLoginConfig(ssoConfigDto);
			return new SsoConfigVo(ssoConfigDto, loginConfig);
		}).collect(Collectors.toList());
		return JsonResultMessage.responseSuccessData(results);
	}

	@ResponseBody
	@PostMapping(value = "/onlineSso")
	public JsonResultMessage<OnlineSsoVo> onlineSso() {
		String url = StringUtils.EMPTY;
		if (simpleSsoConfigProperties.isOnlineSso()) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
				CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
				CompanySsoConfigResponse ssoConfigResponse = companySsoProvider.getSsoConfigResponse(userDetails.getCompanyCode());
				if (ssoConfigResponse.getCompanySimpleAccount() != null) {
					String[] loginNames = userDetails.getUsername().split("\\s*,\\s*");
					String jwtToken = EncryptionUtils.generateJwtToken(userDetails.getCompanyCode(), loginNames[1],
							ssoConfigResponse.getCompanySimpleAccount().getPassword());
					try {
						URIBuilder uriBuilder = new URIBuilder(simpleSsoConfigProperties.getOnlineSsoUrl());
						uriBuilder.addParameter(simpleSsoConfigProperties.getOnlineSsoTokenName(), jwtToken);
						url = uriBuilder.toString();
					} catch (URISyntaxException e) {
						logger.error("URL配置错误", e);
					}
				}
			} else {
				url = simpleSsoConfigProperties.getOnlineSsoUrl();
			}
		}
		return JsonResultMessage.responseSuccessData(new OnlineSsoVo(url));
	}
}
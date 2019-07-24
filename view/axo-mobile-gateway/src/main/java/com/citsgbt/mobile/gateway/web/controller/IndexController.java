package com.citsgbt.mobile.gateway.web.controller;

import com.citsgbt.mobile.core.utils.spring.MessageSourceUtils;
import com.citsgbt.mobile.core.web.vo.ErrorInfo;
import com.citsgbt.mobile.gateway.config.oauth2.SimpleSsoConfigProperties;
import com.citsgbt.mobile.gateway.security.sso.CompanySsoProvider;
import com.citsgbt.mobile.gateway.security.sso.exception.SsoException;
import com.citsgbt.mobile.gateway.security.sso.utils.Keys;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.WebUtils;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

@Controller
@ApiIgnore
@RequestMapping("/")
public class IndexController {

	private static final Logger logger = getLogger(IndexController.class);

	@Autowired
	private CompanySsoProvider companySsoProvider;

	@Autowired
	private SimpleSsoConfigProperties simpleSsoConfigProperties;

	@GetMapping(value = {"", "index.html", "index", "sso/**"})
	public String index() { // 首页
		String build = ServletUriComponentsBuilder.fromCurrentContextPath().path("/doc.html").build().toUriString();
		logger.info("redirect: {}", build);
		return "redirect:" + build;
	}

	@GetMapping(value = {SSOUtils.SSO_PATH_SSO})
	public String ssoGet(@PathVariable("type") String type, Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig != null && !response.isCommitted()) {
			companySsoProvider.storeStatusAndRedirect(request, response, loginConfig.getInternalClientId(), loginConfig.getRedirectUri());
			return null;
		}
		return "error/error";
	}

	@PostMapping(value = {SSOUtils.SSO_PATH_SSO})
	public String ssoPost(@PathVariable("type") String type, Model model, HttpServletRequest request, HttpServletResponse response) throws IOException {
		return ssoGet(type, model, request, response);
	}

	@GetMapping("/errorPage")
	public String errorPageGet(Model model, HttpServletRequest request, HttpServletResponse response) {
		String errorCode = String.valueOf(request.getAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE));
		Throwable exception = (Throwable) request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
		String requestURI = (String) request.getAttribute(WebUtils.ERROR_REQUEST_URI_ATTRIBUTE);
		ErrorInfo errorInfo;
		if (exception != null) {
			errorInfo = new ErrorInfo(errorCode, exception.getMessage(), ExceptionUtils.getStackTrace(exception));
		} else if (StringUtils.equalsIgnoreCase(errorCode, "404")) {
			errorInfo = new ErrorInfo(errorCode, MessageSourceUtils.getMessage("errors.error.404"),
					StringUtils.join("[", requestURI, "]", MessageSourceUtils.getMessage("errors.error.404")));
		} else {
			errorInfo = new ErrorInfo(errorCode, exception);
		}
		String codeKey = request.getParameter("_k");
		if (StringUtils.isNotBlank(codeKey)) {
			String msg = MessageSourceUtils.getMessage(codeKey);
			if (StringUtils.isNotBlank(msg)) {
				errorInfo = new ErrorInfo(errorCode, msg);
			}
		}
		if(exception instanceof SsoException){
			errorInfo.setLinkUrl(simpleSsoConfigProperties.getDefaultRedirectUri());
		}
		model.addAttribute(Keys.ERROR_INFO_KEY, errorInfo);
		return "error/error";
	}

	@PostMapping("/errorPage")
	public String errorPagePost(Model model, HttpServletRequest request, HttpServletResponse response) {
		return errorPageGet(model, request, response);
	}
}

package com.citsgbt.mobile.open.web.controllers;

import com.citsamex.app.spi.data.base.AbstractServiceParam;
import com.citsamex.app.spi.data.base.CallerResponse;
import com.citsgbt.mobile.core.web.result.JsonResultMessage;
import com.citsgbt.mobile.core.ws.config.appws.AppWsClientConfigProperties;
import com.citsgbt.mobile.core.ws.config.appws.ServiceCallerInfo;
import com.citsgbt.mobile.core.ws.config.appws.ServiceCallerUtils;
import com.citsgbt.mobile.core.ws.config.appws.ServiceLocateKey;
import com.citsgbt.mobile.open.service.caller.ServiceCallerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 直接映射app-ws中的服务路径和服务名称方法，减少开发
 *
 * @author gary.fu
 */
@RestController
@RequestMapping("/service")
public class ServiceCallerController {

	@Autowired
	private AppWsClientConfigProperties appWsClientConfigProperties;

	@Autowired
	private ServiceCallerConfigService serviceCallerConfigService;

	@PostMapping(value = "/call")
	public JsonResultMessage call(@RequestParam(name = "path") String servicePath,
								  @RequestParam(name = "name") String serviceName,
								  @RequestBody(required = false) AbstractServiceParam param) {
		if (appWsClientConfigProperties.getProxyExcludes().contains(ServiceLocateKey.of(servicePath, serviceName))) {
			return JsonResultMessage.responseError("Operation not allowed");
		}
		CallerResponse response = ServiceCallerUtils.invokeDefault(servicePath, serviceName, param, CallerResponse.class);
		return JsonResultMessage.fromCallerResponse(response);
	}

	@GetMapping(value = "/checkParams")
	public JsonResultMessage checkParams(@RequestParam(name = "path") String servicePath,
										 @RequestParam(name = "name") String serviceName) {
		ServiceCallerInfo info = serviceCallerConfigService.locateTargetParam(servicePath, serviceName);
		if (info != null) {
			return JsonResultMessage.responseSuccessData(info.getParamClass() != null ? info.getParamClass().getName() : "NO_PARAMS", "查询成功");
		}
		return JsonResultMessage.responseError("没有找到该服务路径和方法");
	}
}

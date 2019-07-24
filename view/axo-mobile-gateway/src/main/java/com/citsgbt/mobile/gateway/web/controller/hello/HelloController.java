package com.citsgbt.mobile.gateway.web.controller.hello;

import com.citsgbt.mobile.core.web.result.JsonResultMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HelloController {

	@GetMapping(value = "/hello")
	public JsonResultMessage hello(){
		return JsonResultMessage.responseSuccess("Hello world");
	}
}

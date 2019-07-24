package com.citsgbt.mobile.core.web.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RequestMapping("/")
public class IndexController {


	@GetMapping(value = {"", "index.html", "index"})
	public String index() { // 首页
		String build = ServletUriComponentsBuilder.fromCurrentContextPath().path("/doc.html").build().toUriString();
		return "redirect:" + build;
	}

}

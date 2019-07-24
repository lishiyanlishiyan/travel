package com.citsgbt.mobile.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
@SpringBootApplication(exclude = {RedisAutoConfiguration.class, SecurityAutoConfiguration.class})
public class AxoMobileApplication {

	@GetMapping(value = {"", "/"})
	public String index() { // 首页
		String build = ServletUriComponentsBuilder.fromCurrentContextPath().path("/index.html").build().toUriString();
		return "redirect:" + build;
	}

	@Bean
	public FilterRegistrationBean forwardedHeaderFilter() {
		FilterRegistrationBean filterRegBean = new FilterRegistrationBean();
		filterRegBean.setFilter(new ForwardedHeaderFilter());
		filterRegBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return filterRegBean;
	}

	public static void main(String[] args) {
		SpringApplication.run(AxoMobileApplication.class, args);
	}
}

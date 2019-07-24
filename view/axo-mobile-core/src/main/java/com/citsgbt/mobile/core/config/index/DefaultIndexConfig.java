package com.citsgbt.mobile.core.config.index;

import com.citsgbt.mobile.core.web.controllers.IndexController;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

public class DefaultIndexConfig {

	@Bean
	public IndexController indexController() {
		return new IndexController();
	}

	@Bean
	public FilterRegistrationBean forwardedHeaderFilter() {
		FilterRegistrationBean filterRegBean = new FilterRegistrationBean();
		filterRegBean.setFilter(new ForwardedHeaderFilter());
		filterRegBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return filterRegBean;
	}
}

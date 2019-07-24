package com.citsgbt.mobile.gateway.config;

import com.citsgbt.mobile.core.ws.config.sso.SsoParamConfigProperties;
import com.citsgbt.mobile.gateway.config.oauth2.SimpleSsoConfigProperties;
import com.citsgbt.mobile.gateway.web.filters.LocaleRequestContextFilter;
import com.citsgbt.mobile.gateway.web.resovler.CustomCookieLocaleResolver;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.LocaleResolver;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@EnableConfigurationProperties({SimpleSsoConfigProperties.class, SsoParamConfigProperties.class})
@Configuration
public class MobileApplicationConfig {

	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasenames("messages");
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setUseCodeAsDefaultMessage(true);
		return messageSource;
	}

	/**
	 * Session/Cookie级别的Locale设定
	 *
	 * @return
	 */
	@Bean
	public LocaleResolver localeResolver() {
		CustomCookieLocaleResolver localeResolver = new CustomCookieLocaleResolver();
		localeResolver.setCookieName("axo-mobile_localeName");
		localeResolver.setDefaultLocale(LocaleUtils.toLocale("zh_CN"));
		localeResolver.setParamName("axo_mobile_locale");
		localeResolver.setCookieHttpOnly(true);
		return localeResolver;
	}

	@Bean
	public LocaleRequestContextFilter localeRequestContextFilter() {
		LocaleRequestContextFilter localeRequestContextFilter = new LocaleRequestContextFilter();
		localeRequestContextFilter.setLocaleResolver(localeResolver());
		return localeRequestContextFilter;
	}

	@Bean
	public CorsFilter corsFilter() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowedMethods(Stream.of(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.POST, HttpMethod.OPTIONS)
				.map(Enum::name).collect(Collectors.toList()));
		config.setAllowCredentials(true);
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.setMaxAge(7 * 24 * 60 * 60L); // 设置跨域check缓存时间
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}
}

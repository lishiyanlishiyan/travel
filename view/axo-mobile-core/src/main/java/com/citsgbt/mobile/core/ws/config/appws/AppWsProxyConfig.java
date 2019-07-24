package com.citsgbt.mobile.core.ws.config.appws;

import com.citsamex.app.spi.interfaces.TargetService;
import com.citsgbt.mobile.core.ws.config.appws.proxy.ServiceCallerBeansScannerConfigurer;
import org.springframework.context.annotation.Bean;

public class AppWsProxyConfig {

	@Bean
	public ServiceCallerBeansScannerConfigurer serviceCallerBeansScannerConfigurer() {
		ServiceCallerBeansScannerConfigurer serviceCallerBeansScannerConfigurer = new ServiceCallerBeansScannerConfigurer();
		serviceCallerBeansScannerConfigurer.setBasePackages("com.citsamex.app.spi.interfaces.targets");
		serviceCallerBeansScannerConfigurer.setMarkerInterface(TargetService.class);
		return serviceCallerBeansScannerConfigurer;
	}
}

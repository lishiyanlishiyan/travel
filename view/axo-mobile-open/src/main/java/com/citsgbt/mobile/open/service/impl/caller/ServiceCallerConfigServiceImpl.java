package com.citsgbt.mobile.open.service.impl.caller;

import com.citsamex.app.spi.annotation.CallerServiceName;
import com.citsamex.app.spi.annotation.CallerServicePath;
import com.citsamex.app.spi.data.dto.base.CommonDto;
import com.citsamex.app.spi.interfaces.TargetService;
import com.citsgbt.mobile.core.ws.config.appws.ServiceCallerInfo;
import com.citsgbt.mobile.core.ws.config.appws.ServiceLocateKey;
import com.citsgbt.mobile.open.service.caller.ServiceCallerConfigService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ServiceCallerConfigServiceImpl implements ServiceCallerConfigService, InitializingBean {

	@Autowired
	private List<TargetService> targetServices;
	/**
	 *
	 */
	private final Map<ServiceLocateKey, ServiceCallerInfo> serviceInfoMap = new HashMap<>();

	@Override
	public ServiceCallerInfo locateTargetParam(String servicePath, String serviceName) {
		return serviceInfoMap.get(new ServiceLocateKey(servicePath, serviceName));
	}

	@Override
	public void afterPropertiesSet() {
		for (TargetService targetService : targetServices) {
			// 获取配置的path
			CallerServicePath callerServicePath = AnnotationUtils.findAnnotation(targetService.getClass(), CallerServicePath.class);
			if (callerServicePath != null) {
				String servicePath = callerServicePath.value();
				// 解析方法，并获取配置的服务名称
				for (Method method : targetService.getClass().getDeclaredMethods()) {
					CallerServiceName callerServiceName = AnnotationUtils.findAnnotation(method, CallerServiceName.class);
					if (callerServiceName != null) {
						String serviceName = callerServiceName.value(); // 配置的名称
						ServiceLocateKey key = new ServiceLocateKey(servicePath, serviceName); // 包装path和name得到key
						// 默认只解析一个参数
						Class<?> paramClass = null;
						if (method.getParameterTypes().length > 0) {
							for (Class<?> clazz : method.getParameterTypes()) {
								if (CommonDto.class.isAssignableFrom(clazz) || String.class.isAssignableFrom(clazz)) {
									paramClass = clazz;
								}
								if (paramClass != null) {
									break;
								}
							}
						}
						serviceInfoMap.put(key, new ServiceCallerInfo(key, targetService, method, paramClass, method.getParameterTypes()));
					}
				}
			}
		}
	}
}

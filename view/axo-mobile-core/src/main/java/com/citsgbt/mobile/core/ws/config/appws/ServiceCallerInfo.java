package com.citsgbt.mobile.core.ws.config.appws;

import com.citsamex.app.spi.interfaces.TargetService;

import java.lang.reflect.Method;

public class ServiceCallerInfo  {

	private ServiceLocateKey key;

	private TargetService targetService;

	private Method targetMethod;

	private Class<?> paramClass;

	private Class<?>[] paramTypes;

	public ServiceCallerInfo(ServiceLocateKey key, TargetService targetService, Method targetMethod, Class<?> paramClass, Class<?>[] paramTypes) {
		this.key = key;
		this.targetService = targetService;
		this.targetMethod = targetMethod;
		this.paramClass = paramClass;
		this.paramTypes = paramTypes;
	}

	public TargetService getTargetService() {
		return targetService;
	}

	public void setTargetService(TargetService targetService) {
		this.targetService = targetService;
	}

	public Method getTargetMethod() {
		return targetMethod;
	}

	public void setTargetMethod(Method targetMethod) {
		this.targetMethod = targetMethod;
	}

	public Class<?> getParamClass() {
		return paramClass;
	}

	public void setParamClass(Class<?> paramClass) {
		this.paramClass = paramClass;
	}

	public Class<?>[] getParamTypes() {
		return paramTypes;
	}

	public void setParamTypes(Class<?>[] paramTypes) {
		this.paramTypes = paramTypes;
	}

	public ServiceLocateKey getKey() {
		return key;
	}

	public void setKey(ServiceLocateKey key) {
		this.key = key;
	}
}

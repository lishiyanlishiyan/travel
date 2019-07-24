package com.citsgbt.mobile.core.ws.config.appws.proxy;

import com.citsamex.app.spi.annotation.CallerServiceName;
import com.citsamex.app.spi.annotation.CallerServicePath;
import com.citsamex.app.spi.data.base.AbstractServiceParam;
import com.citsamex.app.spi.data.base.CallerRequest;
import com.citsgbt.mobile.core.ws.config.appws.ServiceCallerUtils;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by Gary Fu
 */
class ServiceCallerProxyFactoryBean<T> extends AbstractFactoryBean<T> implements InvocationHandler {

	private Class<T> targetClass;

	public ServiceCallerProxyFactoryBean(Class<T> targetClass) {
		this.targetClass = targetClass;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (Object.class.equals(method.getDeclaringClass())) {
			return method.invoke(this, args);
		}
		Class<T> interfaceCls = getTargetClass();
		// 获取配置的path
		CallerServicePath callerServicePath = AnnotationUtils.findAnnotation(interfaceCls, CallerServicePath.class);
		CallerServiceName callerServiceName = AnnotationUtils.findAnnotation(method, CallerServiceName.class);
		CallerRequest request = new CallerRequest();
		if (args != null && args.length > 0 && args[0] instanceof AbstractServiceParam) {
			request.setServiceParamObj((AbstractServiceParam) args[0]);
		}
		request.setServicePath(callerServicePath.value());
		request.setServiceName(callerServiceName.value());
		request.setVersion(callerServiceName.version());
		return ServiceCallerUtils.invokeDefault(request, method.getReturnType());
	}

	@Override
	public Class<T> getObjectType() {
		return getTargetClass();
	}

	@Override
	protected T createInstance() {
		return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{getTargetClass()}, this);
	}

	private Class<T> getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(Class<T> targetClass) {
		this.targetClass = targetClass;
	}
}

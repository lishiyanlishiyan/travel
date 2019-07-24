package com.citsgbt.mobile.core.ws.config;

import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.common.gzip.GZIPInInterceptor;
import org.apache.cxf.transport.common.gzip.GZIPOutInterceptor;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2016/3/23 15:38.<br>
 *
 * @author gary.fu
 */
public abstract class BaseWebServiceConfig {

	private static final int CXF_CLIENT_CONNECT_TIMEOUT = 30 * 1000;

	private static final int CXF_CLIENT_RECEIVE_TIMEOUT = 2 * 60 * 1000;

	@Autowired(required = false)
	protected LoggingInInterceptor loggingInInterceptor;

	@Autowired(required = false)
	protected LoggingOutInterceptor loggingOutInterceptor;

	private LoggingInInterceptor getLoggingInInterceptor() {
		if (loggingInInterceptor == null) {
			loggingInInterceptor = new LoggingInInterceptor();
		}
		return loggingInInterceptor;
	}

	private LoggingOutInterceptor getLoggingOutInterceptor() {
		if (loggingOutInterceptor == null) {
			loggingOutInterceptor = new LoggingOutInterceptor();
		}
		return loggingOutInterceptor;
	}

	protected JaxWsProxyFactoryBean getFactory(String address, Class className) {
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setAddress(address);//接口地址
		factory.setServiceClass(className);//设置接口类://是通过wsdl2java反生成的代码内接口类类型
		return factory;
	}

	protected void configInterceptor(InterceptorProvider client) {
		client.getInInterceptors().add(getLoggingInInterceptor());
		client.getInFaultInterceptors().add(getLoggingInInterceptor());
		client.getOutInterceptors().add(getLoggingOutInterceptor());
		client.getOutFaultInterceptors().add(getLoggingOutInterceptor());
	}

	protected void configHttpConduit(HTTPConduit conduit) {
		HTTPClientPolicy policy = new HTTPClientPolicy();
		policy.setConnectionTimeout(getConnectTimeout());
		policy.setReceiveTimeout(getSocketTimeout());
		conduit.setClient(policy);
	}

	private Integer getConnectTimeout(){
		return CXF_CLIENT_CONNECT_TIMEOUT;
	}

	private Integer getSocketTimeout(){
		return CXF_CLIENT_RECEIVE_TIMEOUT;
	}

	/**
	 * 客户端对接口方法参数的兼容性设置<br />
	 * 使用CXF实现webservice服务时C/S兼容性的处理<br />
	 * 服务端返回了额外的字段<br />
	 * 这可能是由于服务更新之后，有些新的客户端需要返回额外的字段<br />
	 * 在客户端使用的JaxWsProxyFactoryBean中设置set-jaxb-validation-event-handler属性<br />
	 *
	 * @param factory
	 */
	protected void setJaxbValidationEventHandler(JaxWsProxyFactoryBean factory) {
		Map properties = factory.getProperties();
		if (properties == null) {
			properties = new HashMap();
			factory.setProperties(properties);
		}
		properties.put("set-jaxb-validation-event-handler", Boolean.FALSE);
	}

	/**
	 * 配置客户端接收压缩与返回压缩数据配置
	 *
	 * @param interceptorProvider
	 * @author
	 * @date
	 */
	protected void configCxfGzip(InterceptorProvider interceptorProvider) {
		GZIPInInterceptor gzipInInterceptor = new GZIPInInterceptor();
		GZIPOutInterceptor gzipOutInterceptor = new GZIPOutInterceptor();
		interceptorProvider.getInInterceptors().add(gzipInInterceptor);
		interceptorProvider.getInFaultInterceptors().add(gzipInInterceptor);
		interceptorProvider.getOutInterceptors().add(gzipOutInterceptor);
		interceptorProvider.getOutFaultInterceptors().add(gzipOutInterceptor);
	}

}

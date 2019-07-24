package com.citsgbt.mobile.core.ws.config.appws;

import com.citsamex.app.spi.interfaces.caller.ServiceCaller;
import com.citsgbt.connect.encrypt.appws.AppDataEncryptProvider;
import com.citsgbt.mobile.core.ws.config.BaseWebServiceConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.Client;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.ws.rs.core.MediaType;
import java.util.Collections;

@EnableConfigurationProperties(AppWsClientConfigProperties.class)
@Import(AppDataEncryptConfig.class)
public class AppWsWebServiceConfig extends BaseWebServiceConfig {

	@Autowired
	private AppWsClientConfigProperties appWsClientConfigProperties;

	@Autowired
	private AppDataEncryptProvider appDataEncryptProvider;

	@Bean
	public ServiceCaller serviceCaller() {
		ServiceCaller client = JAXRSClientFactory.create(appWsClientConfigProperties.getWsAddress(),
				ServiceCaller.class, Collections.singletonList(jacksonJaxbJsonProvider()));
		if (client instanceof Client) {
			// Json格式/Xml格式
			if (StringUtils.equalsIgnoreCase("json", appWsClientConfigProperties.getFormat())) {
				((Client) client).type(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE);
			} else {
				((Client) client).type(MediaType.APPLICATION_XML_TYPE).accept(MediaType.APPLICATION_XML_TYPE);
			}
		}
		ClientConfiguration clientConfiguration = WebClient.getConfig(client);
		// 添加http超时控制
		HTTPConduit httpConduit = clientConfiguration.getHttpConduit();
		HTTPClientPolicy policy = new HTTPClientPolicy();
		policy.setConnectionTimeout(appWsClientConfigProperties.getConnectTimeout());
		policy.setReceiveTimeout(appWsClientConfigProperties.getSocketTimeout());
		httpConduit.setClient(policy);
		configInterceptor(clientConfiguration);
		configCxfGzip(clientConfiguration);
		return client;
	}

	@Bean
	public JacksonJaxbJsonProvider jacksonJaxbJsonProvider() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		JacksonJaxbJsonProvider jacksonJaxbJsonProvider = new JacksonJaxbJsonProvider();
		jacksonJaxbJsonProvider.setMapper(mapper);
		jacksonJaxbJsonProvider.setAnnotationsToUse(JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
		return jacksonJaxbJsonProvider;
	}

	@Bean
	public ServiceCallerUtils serviceCallerUtils() {
		ServiceCallerUtils serviceCallerUtils = new ServiceCallerUtils();
		serviceCallerUtils.inject(serviceCaller(), appWsClientConfigProperties, appDataEncryptProvider);
		return serviceCallerUtils;
	}
}

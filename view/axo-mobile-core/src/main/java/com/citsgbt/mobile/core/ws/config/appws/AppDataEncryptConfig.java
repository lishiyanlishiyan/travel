package com.citsgbt.mobile.core.ws.config.appws;

import com.citsgbt.connect.encrypt.appws.AppDataEncryptProvider;
import com.citsgbt.connect.encrypt.appws.AppDataEncryptProviderImpl;
import com.citsgbt.connect.encrypt.provider.EncryptProvider;
import com.citsgbt.connect.encrypt.provider.SimpleEncryptProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

public class AppDataEncryptConfig {

	@Value("classpath:/encrypt/DataEncryptConfigs.xml")
	private Resource configXml;

	@Autowired
	private AppWsClientConfigProperties appWsClientConfigProperties;

	protected boolean isEncryptEnabled(){
		return appWsClientConfigProperties.isEncryptEnabled();
	}

	@Bean
	public EncryptProvider encryptProvider() {
		SimpleEncryptProvider simpleEncryptProvider = new SimpleEncryptProvider();
		simpleEncryptProvider.setPassword(appWsClientConfigProperties.getEncryptPassword());
		return simpleEncryptProvider;
	}

	@Bean
	public AppDataEncryptProvider appDataEncryptProvider() {
		AppDataEncryptProviderImpl appDataEncryptProvider = new AppDataEncryptProviderImpl();
		appDataEncryptProvider.setConfigXml(configXml);
		appDataEncryptProvider.setEncryptProvider(encryptProvider());
		appDataEncryptProvider.setEnabled(isEncryptEnabled());
		return appDataEncryptProvider;
	}
}

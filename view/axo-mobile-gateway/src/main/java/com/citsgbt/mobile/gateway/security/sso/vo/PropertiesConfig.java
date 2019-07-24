package com.citsgbt.mobile.gateway.security.sso.vo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.Properties;
import java.util.Set;

public class PropertiesConfig {

	private static final Logger logger = LoggerFactory.getLogger(PropertiesConfig.class);

	private Properties configProperties;

	public PropertiesConfig(String fileName) {
		configProperties = new Properties();
		try {
			Resource resource = new FileSystemResource("config/" + fileName);
			logger.info("sso config path: {}", resource.getURI());
			if (!resource.isReadable()) {
				resource = new ClassPathResource(fileName);
			}
			configProperties.load(resource.getInputStream());
		} catch (Exception e) {
			logger.error("加载配置文件错误", e);
		}
	}

	private String getConfigPropertyValue(String key) {
		if (configProperties != null && configProperties.containsKey(key)) {
			return (String) configProperties.get(key);
		} else {
			return null;
		}
	}

	public Properties getConfigProperties() {
		return configProperties;
	}

	public String getPropertyValueIgnoreCase(String keyName, String defaultValue) {
		String value = getPropertyValueIgnoreCase(keyName);
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		return value;
	}

	public String getPropertyValueIgnoreCase(String keyName) {
		String result = getConfigPropertyValue(keyName);
		if (StringUtils.isBlank(result)) {
			Set keySet = configProperties.keySet();
			for (Object o : keySet) {
				String key = (String) o;
				if (key != null && key.equalsIgnoreCase(keyName)) {
					result = configProperties.getProperty(key);
					break;
				}
			}
		}
		return result;
	}

}

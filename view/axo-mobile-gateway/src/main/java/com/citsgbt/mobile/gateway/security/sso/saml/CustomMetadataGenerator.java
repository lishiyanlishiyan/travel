package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.util.URLBuilder;
import org.opensaml.xml.util.Pair;
import org.springframework.security.saml.metadata.MetadataGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * 给metadata 添加companyId
 *
 * @author gary.fu
 */
class CustomMetadataGenerator extends MetadataGenerator {

	@Override
	protected AssertionConsumerService getAssertionConsumerService(String entityBaseURL, String entityAlias, boolean isDefault, int index, String filterURL, String binding) {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		String location = null;
		if (loginConfig instanceof SAMLConfig) {
			SAMLConfig samlConfig = (SAMLConfig) loginConfig;
			if (StringUtils.isNotBlank(samlConfig.getAssertionConsumerServiceURL())) {
				location = samlConfig.getAssertionConsumerServiceURL();
			} else {
				Map<String, String> parameters = new HashMap<>();
				parameters.put("companyId", loginConfig.getCompanyCode());
				if (StringUtils.isNotBlank(loginConfig.getKey())) {
					parameters.put("ssoKey", loginConfig.getKey());
				}
				location = getServerURLCustomized(entityBaseURL, entityAlias, filterURL, parameters);
			}
		}
		AssertionConsumerService consumer;
		if (StringUtils.isNotBlank(location)) {
			SAMLObjectBuilder<AssertionConsumerService> builder = (SAMLObjectBuilder<AssertionConsumerService>) builderFactory.getBuilder(AssertionConsumerService.DEFAULT_ELEMENT_NAME);
			consumer = builder.buildObject();
			consumer.setLocation(location);
			consumer.setBinding(binding);
			if (isDefault) {
				consumer.setIsDefault(true);
			}
			consumer.setIndex(index);
		} else {
			consumer = super.getAssertionConsumerService(entityBaseURL, entityAlias, isDefault, index, filterURL, binding);
		}
		return consumer;
	}

	/**
	 * 拼接Url
	 *
	 * @param entityBaseURL
	 * @param entityAlias
	 * @param processingURL
	 * @param parameters
	 * @return
	 * @see MetadataGenerator#getServerURL(String, String, String, Map)
	 */
    private String getServerURLCustomized(String entityBaseURL, String entityAlias, String processingURL, Map<String, String> parameters) {
		StringBuilder result = new StringBuilder();
		result.append(entityBaseURL);
		if (!processingURL.startsWith("/")) {
			result.append("/");
		}
		result.append(processingURL);
		if (entityAlias != null) {
			if (!processingURL.endsWith("/")) {
				result.append("/");
			}
			result.append("alias/");
			result.append(entityAlias);
		}
		String resultString = result.toString();
		if (parameters == null || parameters.size() == 0) {
			return resultString;
		} else {
			// Add parameters
			URLBuilder returnUrlBuilder = new URLBuilder(resultString);
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				returnUrlBuilder.getQueryParams().add(new Pair<>(entry.getKey(), entry.getValue()));
			}
			return returnUrlBuilder.buildURL();
		}
	}
}

package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.common.SAMLException;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.slf4j.Logger;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.websso.WebSSOProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileOptions;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 构建Request
 *
 * @author gary.fu
 */
class CustomWebSSOProfileImpl extends WebSSOProfileImpl {

	private static final Logger logger = getLogger(CustomWebSSOProfileImpl.class);

	@Override
	protected Issuer getIssuer(String localEntityId) {
		return super.getIssuer(getLocalEntityIssuer(localEntityId));
	}

	/**
	 * 替换LocalEntity为issuer
	 *
	 * @param localEntityId
	 * @return
	 */
	private String getLocalEntityIssuer(String localEntityId) {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof SAMLConfig) {
			SAMLConfig samlConfig = (SAMLConfig) loginConfig;
			if (StringUtils.isNotBlank(samlConfig.getIssuer())) { // 获取Issuer
				return samlConfig.getIssuer();
			}
		}
		return localEntityId;
	}


	@Override
	protected AuthnRequest getAuthnRequest(SAMLMessageContext context, WebSSOProfileOptions options, AssertionConsumerService assertionConsumer, SingleSignOnService bindingService) throws SAMLException, MetadataProviderException {
        //		buildConditions(getLocalEntityIssuer(context.getLocalEntityId()), request); // condition暂不生成
		logger.debug("condition暂不生成{}", context.getLocalEntityId());
		return super.getAuthnRequest(context, options, assertionConsumer, bindingService);
	}

	protected void buildConditions(String entityId, AuthnRequest request) {
		SAMLObjectBuilder<Audience> audienceBuilder = (SAMLObjectBuilder<Audience>) builderFactory.getBuilder(Audience.DEFAULT_ELEMENT_NAME);
		Audience audience = audienceBuilder.buildObject();
		audience.setAudienceURI(entityId);
		SAMLObjectBuilder<AudienceRestriction> arBuilder = (SAMLObjectBuilder<AudienceRestriction>) builderFactory.getBuilder(AudienceRestriction.DEFAULT_ELEMENT_NAME);
		AudienceRestriction ar = arBuilder.buildObject();
		ar.getAudiences().add(audience);
		SAMLObjectBuilder<Conditions> conditionsBuilder = (SAMLObjectBuilder<Conditions>) builderFactory.getBuilder(Conditions.DEFAULT_ELEMENT_NAME);
		Conditions conditions = conditionsBuilder.buildObject();
		conditions.getAudienceRestrictions().add(ar);
		request.setConditions(conditions);
	}
}

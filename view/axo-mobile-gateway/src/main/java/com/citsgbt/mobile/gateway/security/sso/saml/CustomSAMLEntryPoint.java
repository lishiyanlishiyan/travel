package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.websso.WebSSOProfileOptions;

/**
 * 自定义EntryPoint
 */
class CustomSAMLEntryPoint extends SAMLEntryPoint {

	@Override
	protected WebSSOProfileOptions getProfileOptions(SAMLMessageContext context, AuthenticationException exception) throws MetadataProviderException {
		WebSSOProfileOptions profileOptions = super.getProfileOptions(context, exception);
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof SAMLConfig) {
			SAMLConfig samlConfig = (SAMLConfig) loginConfig;
			profileOptions.setRelayState(samlConfig.getRelayState());
			profileOptions.setNameID(samlConfig.getNameIDPolicyFormat());
			profileOptions.setAllowCreate(true);
			boolean verifySign = BooleanUtils.toBoolean(samlConfig.getVerifySignatureNeeded());
			if (verifySign) {
				profileOptions.setForceAuthN(true);
				profileOptions.setProviderName(samlConfig.getProviderName());
				// post方式传递SAMLRequest
				profileOptions.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
			}
		}
		return profileOptions;
	}
}

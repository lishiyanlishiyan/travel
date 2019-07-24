package com.citsgbt.mobile.gateway.security.sso.oauth;

import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.OauthConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 根据LoginConfig计算出oauth client配置
 *
 * @author gary.fu
 */
@Component
class ClientRegistrationRepositoryImpl implements ClientRegistrationRepository {

	private static final Logger logger = getLogger(ClientRegistrationRepositoryImpl.class);

	@Override
	public ClientRegistration findByRegistrationId(String registrationId) {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof OauthConfig) {
			OauthConfig oauthConfig = (OauthConfig) loginConfig;
			logger.debug(oauthConfig.toString());
			return ClientRegistration.withRegistrationId(oauthConfig.getKey())
					.clientId(oauthConfig.getClientId())
					.clientSecret(oauthConfig.getClientSecret())
					.clientName(loginConfig.getKey())
					.authorizationUri(oauthConfig.getAuthorizationUri())
					.authorizationGrantType(StringUtils.isNotBlank(oauthConfig.getAuthorizationGrantType())
							? new AuthorizationGrantType(oauthConfig.getAuthorizationGrantType()) : AuthorizationGrantType.AUTHORIZATION_CODE)
					.scope(oauthConfig.getScope())
					.tokenUri(oauthConfig.getTokenUri())
					.userInfoUri(oauthConfig.getUserInfoUri())
					.userNameAttributeName(oauthConfig.getUserIDAttrName())
					.clientAuthenticationMethod(ClientAuthenticationMethod.POST)
					.redirectUriTemplate(oauthConfig.getOauthRedirectUri())
					.build();
		}
		return null;
	}
}

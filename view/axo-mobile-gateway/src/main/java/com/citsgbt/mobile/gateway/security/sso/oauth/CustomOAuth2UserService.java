package com.citsgbt.mobile.gateway.security.sso.oauth;

import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 获取用户信息
 */
public class CustomOAuth2UserService implements OAuth2UserService {

	private static final String MISSING_USER_INFO_URI_ERROR_CODE = "missing_user_info_uri";
	private static final String MISSING_USER_NAME_ATTRIBUTE_ERROR_CODE = "missing_user_name_attribute";
	private static final String INVALID_USER_INFO_RESPONSE_ERROR_CODE = "invalid_user_info_response";
	private final GenericHttpMessageConverter genericHttpMessageConverter = new MappingJackson2HttpMessageConverter();

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) {
		Assert.notNull(userRequest, "userRequest cannot be null");

		if (!StringUtils.hasText(userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri())) {
			OAuth2Error oauth2Error = new OAuth2Error(
					MISSING_USER_INFO_URI_ERROR_CODE,
					"Missing required UserInfo Uri in UserInfoEndpoint for Client Registration: " +
							userRequest.getClientRegistration().getRegistrationId(),
					null
			);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
		}
		String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
		if (!StringUtils.hasText(userNameAttributeName)) {
			OAuth2Error oauth2Error = new OAuth2Error(
					MISSING_USER_NAME_ATTRIBUTE_ERROR_CODE,
					"Missing required \"user name\" attribute name in UserInfoEndpoint for Client Registration: " +
							userRequest.getClientRegistration().getRegistrationId(),
					null
			);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
		}

		ParameterizedTypeReference<Map<String, Object>> typeReference =
				new ParameterizedTypeReference<Map<String, Object>>() {
				};
		Map<String, Object> userAttributes = this.getUserInfoResponse(userRequest, typeReference);
		GrantedAuthority authority = new OAuth2UserAuthority(userAttributes);
		Set<GrantedAuthority> authorities = new HashSet<>();
		authorities.add(authority);

		return new DefaultOAuth2User(authorities, userAttributes, userNameAttributeName);
	}

	private <T> T getUserInfoResponse(OAuth2UserRequest userRequest, ParameterizedTypeReference<T> typeReference) {
		ClientHttpResponse infoResponse = getUserInfoResponse(userRequest.getClientRegistration(), userRequest.getAccessToken());
		try {
			return (T) this.genericHttpMessageConverter.read(typeReference.getType(), null, infoResponse);
		} catch (IOException | HttpMessageNotReadableException ex) {
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE,
					"An error occurred reading the UserInfo Success response: " + ex.getMessage(), null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
		}
	}

	private ClientHttpResponse getUserInfoResponse(ClientRegistration clientRegistration,
                                                   OAuth2AccessToken oauth2AccessToken) {
		URI userInfoUri = URI.create(clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri());
		BearerAccessToken accessToken = new BearerAccessToken(oauth2AccessToken.getTokenValue());

		UserInfoRequest userInfoRequest = new UserInfoRequest(userInfoUri, HTTPRequest.Method.POST, accessToken);
		HTTPRequest httpRequest = userInfoRequest.toHTTPRequest();
		httpRequest.setAccept(MediaType.APPLICATION_JSON_VALUE);
		httpRequest.setConnectTimeout(30000);
		httpRequest.setReadTimeout(30000);
		httpRequest.setAuthorization(accessToken.toAuthorizationHeader());
		SSOUtils.processOauthAdditionalHeaders(httpRequest);
		HTTPResponse httpResponse;
		try {
			httpResponse = httpRequest.send();
		} catch (IOException ex) {
			throw new AuthenticationServiceException("An error occurred while sending the UserInfo Request: " +
					ex.getMessage(), ex);
		}

		if (httpResponse.getStatusCode() == HTTPResponse.SC_OK) {
			return new CustomClientHttpResponse(httpResponse);
		}

		UserInfoErrorResponse userInfoErrorResponse;
		try {
			userInfoErrorResponse = UserInfoErrorResponse.parse(httpResponse);
		} catch (ParseException ex) {
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE,
					"An error occurred parsing the UserInfo Error response: " + ex.getMessage(), null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
		}
		ErrorObject errorObject = userInfoErrorResponse.getErrorObject();

		StringBuilder errorDescription = new StringBuilder();
		errorDescription.append("An error occurred while attempting to access the UserInfo Endpoint -> ");
		errorDescription.append("Error details: [");
		errorDescription.append("UserInfo Uri: ").append(userInfoUri.toString());
		errorDescription.append(", Http Status: ").append(errorObject.getHTTPStatusCode());
		if (errorObject.getCode() != null) {
			errorDescription.append(", Error Code: ").append(errorObject.getCode());
		}
		if (errorObject.getDescription() != null) {
			errorDescription.append(", Error Description: ").append(errorObject.getDescription());
		}
		errorDescription.append("]");

		OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE, errorDescription.toString(), null);
		throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
	}
}

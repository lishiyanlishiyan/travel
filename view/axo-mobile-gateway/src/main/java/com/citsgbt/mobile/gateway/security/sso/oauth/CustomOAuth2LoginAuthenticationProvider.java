package com.citsgbt.mobile.gateway.security.sso.oauth;

import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.gateway.security.sso.SsoLoginResultVo;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;
import com.citsgbt.mobile.gateway.security.sso.vo.OauthConfig;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.citsgbt.mobile.gateway.security.sso.utils.Keys.SSO_LOGIN_RESULT_KEY;

/**
 * 认证后通过UserDetailService计算本地的用户
 *
 * @author gary.fu
 */
public class CustomOAuth2LoginAuthenticationProvider extends OAuth2LoginAuthenticationProvider {

	private UserDetailsService userDetailsService;

	/**
	 * Constructs an {@code OAuth2LoginAuthenticationProvider} using the provided parameters.
	 *
	 * @param accessTokenResponseClient the client used for requesting the access token credential from the Token Endpoint
	 * @param userService               the service used for obtaining the user attributes of the End-User from the UserInfo Endpoint
	 */
	public CustomOAuth2LoginAuthenticationProvider(OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient,
												   OAuth2UserService<OAuth2UserRequest, OAuth2User> userService) {
		super(accessTokenResponseClient, userService);
	}

	@Override
	public Authentication authenticate(Authentication authentication) {
		OAuth2LoginAuthenticationToken authenticationResult = (OAuth2LoginAuthenticationToken) super.authenticate(authentication);
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof OauthConfig && authenticationResult != null) {
			OauthConfig oauthConfig = (OauthConfig) loginConfig;
			HttpServletRequest request = HttpRequestUtils.getCurrentRequest();
			String loginName = authenticationResult.getName();
			LoginVo loginVo = new LoginVo(oauthConfig.getCompanyCode(), loginName);
			UserDetails userDetails = userDetailsService.loadUserByUsername(loginVo.toUsername());
			Map<String, Object> resultMap = HttpRequestUtils.processInputMap(request);
			SsoLoginResultVo resultVo = new SsoLoginResultVo(oauthConfig, resultMap, loginVo);
			resultVo.setIpAddress(HttpRequestUtils.getIp(request));
			request.setAttribute(SSO_LOGIN_RESULT_KEY, resultVo);
			authenticationResult.setDetails(userDetails);
			DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authenticationResult.getPrincipal();
			Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
			attributes.put(oauthConfig.getUserIDAttrName(), loginVo.toUsername());
            return new OAuth2LoginAuthenticationToken(
                    authenticationResult.getClientRegistration(),
                    authenticationResult.getAuthorizationExchange(),
                    new DefaultOAuth2User(new HashSet<>(userDetails.getAuthorities()), attributes, oauthConfig.getUserIDAttrName()),
                    userDetails.getAuthorities(),
                    authenticationResult.getAccessToken());
		}
		return authenticationResult;
	}

	public UserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}
}

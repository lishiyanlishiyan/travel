package com.citsgbt.mobile.gateway.security.sso;

import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.core.utils.spring.ApplicationContextUtils;
import com.citsgbt.mobile.core.utils.spring.MessageSourceUtils;
import com.citsgbt.mobile.gateway.security.sso.cas.CustomCas20ServiceTicketValidator;
import com.citsgbt.mobile.gateway.security.sso.cas.CustomCasAssertionUserDetailsServiceImpl;
import com.citsgbt.mobile.gateway.security.sso.cas.CustomProxyTicketValidator;
import com.citsgbt.mobile.gateway.security.sso.exception.SsoException;
import com.citsgbt.mobile.gateway.security.sso.saml.CustomSAMLAuthenticationProvider;
import com.citsgbt.mobile.gateway.security.sso.saml.SAMLUserDetailsServiceImpl;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.CASConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.OauthConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;
import org.jasig.cas.client.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLAuthenticationToken;
import org.springframework.security.saml.log.SAMLLogger;
import org.springframework.security.saml.websso.WebSSOProfileConsumer;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * sso AuthenticationProvider
 * 处理CAS，SAML,WebService以及原来jwt登陆逻辑
 *
 * @author gary.fu
 */
public class CustomSsoAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(CustomSsoAuthenticationProvider.class);

    private UserDetailsService userDetailsService;

    private PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider;

    private OAuth2LoginAuthenticationProvider oauth2LoginAuthenticationProvider;

    @Override
    public Authentication authenticate(Authentication authentication) {
        LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
        try {
            if (loginConfig instanceof CASConfig) { // CAS登陆
                CasAuthenticationProvider casAuthenticationProvider = getCasAuthenticationProvider((CASConfig) loginConfig);
                if (casAuthenticationProvider.supports(authentication.getClass())) {
                    return casAuthenticationProvider.authenticate(authentication);
                }
            } else if (loginConfig instanceof SAMLConfig) { // SAML登陆
                SAMLAuthenticationProvider samlAuthenticationProvider = getSAMLAuthenticationProvider((SAMLConfig) loginConfig);
                if (samlAuthenticationProvider.supports(authentication.getClass())) {
                    return samlAuthenticationProvider.authenticate(authentication);
                }
            } else if (loginConfig instanceof OauthConfig) { // Oauth登陆
                if (getOauth2LoginAuthenticationProvider().supports(authentication.getClass())) {
                    OAuth2LoginAuthenticationToken oAuth2AuthenticationToken = (OAuth2LoginAuthenticationToken) authentication;
                    OAuth2AuthorizationResponse authorizationResponse = oAuth2AuthenticationToken.getAuthorizationExchange().getAuthorizationResponse();
                    OAuth2AuthorizationRequest authorizationRequest = oAuth2AuthenticationToken.getAuthorizationExchange().getAuthorizationRequest();
                    Field redirectUriField = ReflectionUtils.findField(OAuth2AuthorizationResponse.class, "redirectUri", String.class);
                    redirectUriField.setAccessible(true);
                    redirectUriField.set(authorizationResponse, authorizationRequest.getRedirectUri());
                    return getOauth2LoginAuthenticationProvider().authenticate(authentication);
                }
            } else if (authentication instanceof PreAuthenticatedAuthenticationToken) { // 其他单点登陆
                return getPreAuthenticatedAuthenticationProvider().authenticate(authentication);
            }
        } catch (UsernameNotFoundException | AuthenticationServiceException e) {
            throw new SsoException(MessageSourceUtils.getMessage("sso.login.failed"), e, loginConfig);
        } catch (AuthenticationException e) {
            throw new InternalAuthenticationServiceException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error("特殊处理redirectUri错误", e);
        }
        return null;
    }

    /**
     * SAML provider
     *
     * @param samlConfig
     * @return
     */
    private SAMLAuthenticationProvider getSAMLAuthenticationProvider(SAMLConfig samlConfig) {
        SAMLAuthenticationProvider samlAuthenticationProvider = new CustomSAMLAuthenticationProvider();
        samlAuthenticationProvider.setUserDetails(new SAMLUserDetailsServiceImpl(userDetailsService, samlConfig));
        SAMLLogger samlLogger = ApplicationContextUtils.getBean("samlLogger", SAMLLogger.class);
        WebSSOProfileConsumer webSSOprofileConsumer = ApplicationContextUtils.getBean("webSSOprofileConsumer", WebSSOProfileConsumer.class);
        WebSSOProfileConsumer hokWebSSOprofileConsumer = ApplicationContextUtils.getBean("hokWebSSOprofileConsumer", WebSSOProfileConsumer.class);
        samlAuthenticationProvider.setSamlLogger(samlLogger);
        samlAuthenticationProvider.setConsumer(webSSOprofileConsumer);
        samlAuthenticationProvider.setHokConsumer(hokWebSSOprofileConsumer);
        samlAuthenticationProvider.setForcePrincipalAsString(false);
        return samlAuthenticationProvider;
    }

    /**
     * CAS provider
     *
     * @param casConfig
     * @return
     */
    private CasAuthenticationProvider getCasAuthenticationProvider(CASConfig casConfig) {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setAuthenticationUserDetailsService(new CustomCasAssertionUserDetailsServiceImpl(userDetailsService, casConfig));
        casAuthenticationProvider.setKey(UUID.randomUUID().toString());
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(casConfig.getServerName());
        String serviceUrl = CommonUtils.constructServiceUrl(HttpRequestUtils.getCurrentRequest(), HttpRequestUtils.getCurrentResponse(),
                null, serviceProperties.getService(),
                serviceProperties.getServiceParameter(),
                serviceProperties.getArtifactParameter(),
                false);
        serviceProperties.setService(serviceUrl);
        CustomCas20ServiceTicketValidator ticketValidator = new CustomCas20ServiceTicketValidator(casConfig.getCasServerUrlPrefix());
        casAuthenticationProvider.setServiceProperties(serviceProperties);
        CustomProxyTicketValidator proxyTicketValidator = new CustomProxyTicketValidator(ticketValidator);
        casAuthenticationProvider.setTicketValidator(proxyTicketValidator);
        return casAuthenticationProvider;
    }

    /**
     * 支持CAS，SAML, PreAuthenticatedAuthenticationToken 验证
     *
     * @param authentication
     * @return
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication))
                || (CasAuthenticationToken.class.isAssignableFrom(authentication))
                || (CasAssertionAuthenticationToken.class.isAssignableFrom(authentication))
                || (SAMLAuthenticationToken.class.isAssignableFrom(authentication))
                || (PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication))
                || (OAuth2LoginAuthenticationToken.class.isAssignableFrom(authentication));
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void setPreAuthenticatedAuthenticationProvider(PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider) {
        this.preAuthenticatedAuthenticationProvider = preAuthenticatedAuthenticationProvider;
    }

    /**
     * 预认证Provider
     *
     * @return
     */
    private PreAuthenticatedAuthenticationProvider getPreAuthenticatedAuthenticationProvider() {
        return preAuthenticatedAuthenticationProvider;
    }

    private OAuth2LoginAuthenticationProvider getOauth2LoginAuthenticationProvider() {
        return oauth2LoginAuthenticationProvider;
    }

    public void setOauth2LoginAuthenticationProvider(OAuth2LoginAuthenticationProvider oauth2LoginAuthenticationProvider) {
        this.oauth2LoginAuthenticationProvider = oauth2LoginAuthenticationProvider;
    }
}

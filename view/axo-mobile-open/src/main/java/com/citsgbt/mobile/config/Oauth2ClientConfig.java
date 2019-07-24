package com.citsgbt.mobile.config;

import com.citsgbt.mobile.core.ws.config.sso.SsoParamConfigProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2RestOperationsConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Collections;

/**
 * 配置oauth2客户端
 *
 * @author gary.fu
 */
@Configuration
@EnableOAuth2Client
@EnableConfigurationProperties({SsoParamConfigProperties.class})
@Import(OAuth2RestOperationsConfiguration.class)
public class Oauth2ClientConfig {

    /**
     * 使用password模式，需要使用request scope
     *
     * @param auth2ClientContext
     * @return
     */
    @Bean
    @RequestScope
    public OAuth2RestTemplate oauth2RestTemplate(OAuth2ClientContext auth2ClientContext, ResourceOwnerPasswordResourceDetails resourceOwnerPasswordResourceDetails) {
        OAuth2RestTemplate template = new OAuth2RestTemplate(resourceOwnerPasswordResourceDetails, auth2ClientContext);
        ResourceOwnerPasswordAccessTokenProvider resourceOwnerPasswordAccessTokenProvider = new ResourceOwnerPasswordAccessTokenProvider();
        AccessTokenProviderChain provider = new AccessTokenProviderChain(
                Collections.singletonList(resourceOwnerPasswordAccessTokenProvider));
        template.setAccessTokenProvider(provider);
        return template;
    }

    @Bean
    @ConfigurationProperties(prefix = "security.oauth2.client")
    @RequestScope
    public ResourceOwnerPasswordResourceDetails resourceOwnerPasswordResourceDetails() {
        return new ResourceOwnerPasswordResourceDetails();
    }

}

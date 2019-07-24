package com.citsgbt.mobile.gateway.config.oauth2;

import com.citsgbt.mobile.core.client.SimpleClient;
import com.citsgbt.mobile.core.utils.encoder.NoOpPasswordEncoder;
import com.citsgbt.mobile.core.ws.config.sso.SsoParamConfigProperties;
import com.citsgbt.mobile.gateway.consts.OAuth2Consts;
import com.citsgbt.mobile.gateway.security.sso.oauth.CustomJwtAccessTokenConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import static com.citsgbt.mobile.core.utils.lang.CoreUtils.parseConfig;

@Configuration
@EnableAuthorizationServer
@SuppressWarnings("squid:S3305") // see Exceptions
class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

	@Autowired
	@Qualifier("authenticationManagerBean")
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private SimpleSsoConfigProperties simpleSsoConfigProperties;

	@Autowired
	private SsoParamConfigProperties ssoParamConfigProperties;

	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		InMemoryClientDetailsServiceBuilder clientDetailsServiceBuilder = clients.inMemory();
		for (SimpleClient client : simpleSsoConfigProperties.getClients()) {
			ClientDetailsServiceBuilder<InMemoryClientDetailsServiceBuilder>.ClientBuilder clientBuilder = clientDetailsServiceBuilder
					.withClient(client.getClientId())
					.secret(client.getClientSecret())
					.resourceIds(parseConfig(client.getResourceIds(), OAuth2Consts.AXO_RESOURCE_ID))
					.authorizedGrantTypes(parseConfig(client.getGrantTypes()))
					.authorities(parseConfig(client.getAuthorities(), "ROLE_CLIENT"))
					.scopes(parseConfig(client.getScopes(), "all"))
					.autoApprove(client.isAutoApprove())
					.redirectUris(parseConfig(client.getRedirectUris()))
					.accessTokenValiditySeconds(client.getAccessTokenValiditySeconds());
			if (client.getGrantTypes().contains("refresh_token")) {
				clientBuilder.refreshTokenValiditySeconds(client.getRefreshTokenValiditySeconds());
			}
		}
	}

	@Bean
	public NoOpPasswordEncoder passwordEncoder() {
		return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
		endpoints.tokenStore(jwtTokenStore())
				.accessTokenConverter(jwtAccessTokenConverter())
				.authenticationManager(authenticationManager);
	}

	/**
	 * 使用jwt token方式，无状态服务，不用存储token
	 *
	 * @return
	 */
	@Bean
	public JwtTokenStore jwtTokenStore() {
		return new JwtTokenStore(jwtAccessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter jwtAccessTokenConverter() {
		CustomJwtAccessTokenConverter jwtAccessTokenConverter = new CustomJwtAccessTokenConverter();
		DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
		DefaultUserAuthenticationConverter userAuthenticationConverter = new DefaultUserAuthenticationConverter();
		userAuthenticationConverter.setUserDetailsService(userDetailsService);
		accessTokenConverter.setUserTokenConverter(userAuthenticationConverter);
		jwtAccessTokenConverter.setAccessTokenConverter(accessTokenConverter);
		jwtAccessTokenConverter.setRedisTemplate(redisTemplate);
		jwtAccessTokenConverter.setSsoParamConfigProperties(ssoParamConfigProperties);
		return jwtAccessTokenConverter;
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
		oauthServer.allowFormAuthenticationForClients();
	}

}

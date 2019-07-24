package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsgbt.mobile.gateway.config.security.WebSecurityConfig;
import com.citsgbt.mobile.gateway.security.sso.CustomSsoAuthenticationProvider;
import com.citsgbt.mobile.gateway.security.sso.oauth.CustomAuthorizationCodeTokenResponseClient;
import com.citsgbt.mobile.gateway.security.sso.oauth.CustomOAuth2AuthorizationRequestRedirectFilter;
import com.citsgbt.mobile.gateway.security.sso.oauth.CustomOAuth2LoginAuthenticationProvider;
import com.citsgbt.mobile.gateway.security.sso.oauth.CustomOAuth2UserService;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.common.binding.decoding.BasicURLComparator;
import org.opensaml.saml2.binding.decoding.HTTPRedirectDeflateDecoder;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.encoder.MessageEncoder;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DelegatingOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.SAMLWebSSOHoKProcessingFilter;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.*;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.*;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.*;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;

/**
 * https://github.com/vdenotaris/spring-boot-security-saml-sample
 *
 * @author gary.fu
 */
@Configuration
@EnableWebSecurity
public class SecurityWithSsoConfig extends WebSecurityConfig {

	private Timer backgroundTaskTimer;
	private MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager;

	@Autowired
	private ClientRegistrationRepository clientRegistrationRepository;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) {
		// 自定义单点登陆控制
		CustomSsoAuthenticationProvider customSsoAuthenticationProvider = new CustomSsoAuthenticationProvider();
		customSsoAuthenticationProvider.setUserDetailsService(userDetailsServiceLocal);
		customSsoAuthenticationProvider.setPreAuthenticatedAuthenticationProvider(preAuthenticatedAuthenticationProvider());
		customSsoAuthenticationProvider.setOauth2LoginAuthenticationProvider(oAuth2LoginAuthenticationProvider());
		auth.authenticationProvider(customSsoAuthenticationProvider)
				.authenticationProvider(daoAuthenticationProvider());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		http.addFilterAfter(metadataGeneratorFilter(), SecurityContextPersistenceFilter.class)
				.addFilterAfter(metadataDisplayFilter(), SecurityContextPersistenceFilter.class);
	}

	@PostConstruct
	public void init() {
		this.backgroundTaskTimer = new Timer(true);
		this.multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
	}

	@PreDestroy
	public void destroy() {
		this.backgroundTaskTimer.purge();
		this.backgroundTaskTimer.cancel();
		this.multiThreadedHttpConnectionManager.shutdown();
	}

	// Initialization of the velocity engine
	@Bean
	public VelocityEngine velocityEngine() {
		return VelocityFactory.getEngine();
	}

	// XML parser pool needed for OpenSAML parsing
	@Bean(initMethod = "initialize")
	public StaticBasicParserPool parserPool() {
		return new StaticBasicParserPool();
	}

	@Bean(name = "parserPoolHolder")
	public ParserPoolHolder parserPoolHolder() {
		return new ParserPoolHolder();
	}

	// Bindings, encoders and decoders used for creating and parsing messages
	@Bean
	public HttpClient httpClient() {
		return new HttpClient(this.multiThreadedHttpConnectionManager);
	}

	// Provider of default SAML Context
	@Bean
	public CustomSAMLContextProviderImpl contextProvider() {
		return new CustomSAMLContextProviderImpl();
	}

	// Initialization of OpenSAML library
	@Bean
	public static SAMLBootstrap sAMLBootstrap() {
		return new CustomSAMLBootstrap();
	}

	// Logger for SAML messages and events
	@Bean
	public SAMLDefaultLogger samlLogger() {
		return new SAMLDefaultLogger();
	}

	// SAML 2.0 WebSSO Assertion Consumer
	@Bean
	public WebSSOProfileConsumer webSSOprofileConsumer() {
		CustomWebSSOProfileConsumerImpl customWebSSOProfileConsumer = new CustomWebSSOProfileConsumerImpl();
		customWebSSOProfileConsumer.setResponseSkew(120);
		return customWebSSOProfileConsumer;
	}

	// SAML 2.0 Holder-of-Key WebSSO Assertion Consumer
	@Bean
	public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
		return new WebSSOProfileConsumerHoKImpl();
	}

	// SAML 2.0 Web SSO profile
	@Bean
	public WebSSOProfile webSSOprofile() {
		return new CustomWebSSOProfileImpl();
	}

	// SAML 2.0 Holder-of-Key Web SSO profile
	@Bean
	public WebSSOProfileHoKImpl hokWebSSOProfile() {
		return new WebSSOProfileHoKImpl();
	}

	// SAML 2.0 ECP profile
	@Bean
	public WebSSOProfileECPImpl ecpprofile() {
		return new WebSSOProfileECPImpl();
	}

	@Bean
	public SingleLogoutProfile logoutprofile() {
		return new SingleLogoutProfileImpl();
	}

	@Bean
	public WebSSOProfileOptions defaultWebSSOProfileOptions() {
		WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
		webSSOProfileOptions.setIncludeScoping(false);
		return webSSOProfileOptions;
	}

	// Entry point to initialize authentication, default values taken from
	// properties file
	@Bean
	public SAMLEntryPoint samlEntryPoint() {
		CustomSAMLEntryPoint samlEntryPoint = new CustomSAMLEntryPoint();
		samlEntryPoint.setDefaultProfileOptions(defaultWebSSOProfileOptions());
		return samlEntryPoint;
	}

	@Bean
	public KeyManager keyManager() {
		return new CustomSAMLKeyManager();
	}

	// Setup advanced info about metadata
	@Bean
	public ExtendedMetadata extendedMetadata() {
		ExtendedMetadata extendedMetadata = new ExtendedMetadata();
		extendedMetadata.setIdpDiscoveryEnabled(true);
		extendedMetadata.setSignMetadata(false);
		extendedMetadata.setEcpEnabled(true);
		return extendedMetadata;
	}

	@Bean
	@Qualifier("metadata")
	public CachingMetadataManager metadata() throws MetadataProviderException {
		List<MetadataProvider> providers = new ArrayList<>();
		return new CachingMetadataManager(providers);
	}

	@Bean
	public MetadataGenerator metadataGenerator() {
		CustomMetadataGenerator metadataGenerator = new CustomMetadataGenerator();
		metadataGenerator.setEntityId("connect.citsgbt.com");
		metadataGenerator.setIncludeDiscoveryExtension(false);
		metadataGenerator.setKeyManager(keyManager());
		return metadataGenerator;
	}

	@Bean
	public MetadataGeneratorFilter metadataGeneratorFilter() {
		CustomMetadataGeneratorFilter customMetadataGeneratorFilter = new CustomMetadataGeneratorFilter(metadataGenerator());
		customMetadataGeneratorFilter.setBaseEntityId("connect.citsgbt.com");
		return customMetadataGeneratorFilter;
	}

	@Bean
	public MetadataDisplayFilter metadataDisplayFilter() {
		return new CustomMetadataDisplayFilter();
	}

	// Bindings
	private ArtifactResolutionProfile artifactResolutionProfile() {
		final ArtifactResolutionProfileImpl artifactResolutionProfile =
				new ArtifactResolutionProfileImpl(httpClient());
		artifactResolutionProfile.setProcessor(new SAMLProcessorImpl(soapBinding()));
		return artifactResolutionProfile;
	}

	@Bean
	public HTTPArtifactBinding artifactBinding(ParserPool parserPool, VelocityEngine velocityEngine) {
		return new HTTPArtifactBinding(parserPool, velocityEngine, artifactResolutionProfile());
	}

	@Bean
	public HTTPSOAP11Binding soapBinding() {
		return new HTTPSOAP11Binding(parserPool());
	}

	@Bean
	public MessageEncoder httpEncoder() {
		return new CustomHTTPPostEncoder(velocityEngine(), "/templates/saml2-post-binding.vm");
	}

	@Bean
	public HTTPPostBinding httpPostBinding() {
		StaticBasicParserPool parsePool = parserPool();
		CustomHTTPPostDecoder customHTTPPostDecoder = new CustomHTTPPostDecoder(parsePool);
		BasicURLComparator uriComparator = new BasicURLComparator();
		uriComparator.setCaseInsensitive(true);
		customHTTPPostDecoder.setURIComparator(uriComparator);
		return new HTTPPostBinding(parsePool, customHTTPPostDecoder, httpEncoder());
	}

	@Bean
	public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
		return new HTTPRedirectDeflateBinding(new HTTPRedirectDeflateDecoder(parserPool()),
				new CustomHTTPRedirectDeflateEncoder());
	}

	@Bean
	public HTTPSOAP11Binding httpSOAP11Binding() {
		return new HTTPSOAP11Binding(parserPool());
	}

	@Bean
	public HTTPPAOS11Binding httpPAOS11Binding() {
		return new HTTPPAOS11Binding(parserPool());
	}

	// Processor
	@Bean
	public SAMLProcessorImpl processor() {
		Collection<SAMLBinding> bindings = new ArrayList<>();
		bindings.add(httpRedirectDeflateBinding());
		bindings.add(httpPostBinding());
		bindings.add(artifactBinding(parserPool(), velocityEngine()));
		bindings.add(httpSOAP11Binding());
		bindings.add(httpPAOS11Binding());
		return new SAMLProcessorImpl(bindings);
	}

	@Bean
	public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
		CustomSAMLProcessingFilter samlProcessingFilter = new CustomSAMLProcessingFilter();
		samlProcessingFilter.setFilterProcessesUrl("/sso/saml");
		samlProcessingFilter.setAuthenticationFailureHandler(customSsoAuthenticationFailureHandler());
		samlProcessingFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler());
		samlProcessingFilter.setAuthenticationManager(authenticationManager());
		return samlProcessingFilter;
	}

	@Bean
	public SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter() throws Exception {
		SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter = new SAMLWebSSOHoKProcessingFilter();
		samlWebSSOHoKProcessingFilter.setFilterProcessesUrl("/sso/saml");
		samlWebSSOHoKProcessingFilter.setAuthenticationFailureHandler(customSsoAuthenticationFailureHandler());
		samlWebSSOHoKProcessingFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler());
		samlWebSSOHoKProcessingFilter.setAuthenticationManager(authenticationManager());
		return samlWebSSOHoKProcessingFilter;
	}
	/*********************************CAS Config*************************************/
	/**
	 * @return
	 * @throws Exception
	 */
	@Bean
	public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
		CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
		casAuthenticationFilter.setAuthenticationManager(authenticationManager());
		casAuthenticationFilter.setFilterProcessesUrl("/sso/cas/**");
		casAuthenticationFilter.setAuthenticationFailureHandler(customSsoAuthenticationFailureHandler());
		casAuthenticationFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler());
		return casAuthenticationFilter;
	}

	/*********************************Oauth Config*************************************/

	@Bean
	public CustomOAuth2AuthorizationRequestRedirectFilter customOAuth2AuthorizationRequestRedirectFilter() {
		return new CustomOAuth2AuthorizationRequestRedirectFilter(clientRegistrationRepository);
	}

	@Bean
	public OAuth2LoginAuthenticationFilter oAuth2LoginAuthenticationFilter() throws Exception {
		OAuth2LoginAuthenticationFilter oAuth2LoginAuthenticationFilter = new OAuth2LoginAuthenticationFilter(clientRegistrationRepository,
				new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository), "/sso/oauth");
		oAuth2LoginAuthenticationFilter.setAuthenticationManager(authenticationManager());
		oAuth2LoginAuthenticationFilter.setAuthenticationFailureHandler(customSsoAuthenticationFailureHandler());
		oAuth2LoginAuthenticationFilter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler());
		return oAuth2LoginAuthenticationFilter;
	}

	@Bean
	public OAuth2LoginAuthenticationProvider oAuth2LoginAuthenticationProvider() {
		OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient = new CustomAuthorizationCodeTokenResponseClient();
		List<OAuth2UserService<OAuth2UserRequest, OAuth2User>> userServices = new ArrayList<>();
		userServices.add(new CustomOAuth2UserService());
		OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService = new DelegatingOAuth2UserService<>(userServices);
		CustomOAuth2LoginAuthenticationProvider oauth2LoginAuthenticationProvider =
				new CustomOAuth2LoginAuthenticationProvider(accessTokenResponseClient, oauth2UserService);
		oauth2LoginAuthenticationProvider.setUserDetailsService(userDetailsServiceLocal);
		return oauth2LoginAuthenticationProvider;
	}
}

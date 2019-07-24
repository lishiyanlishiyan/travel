package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsamex.app.spi.data.caller.response.company.CompanySsoConfigResponse;
import com.citsgbt.mobile.gateway.security.sso.CompanySsoProvider;
import com.citsgbt.mobile.gateway.security.sso.SsoType;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.metadata.*;
import org.springframework.security.saml.util.SAMLUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * @author gary.fu
 * @see MetadataGeneratorFilter
 */
public class CustomMetadataGeneratorFilter extends MetadataGeneratorFilter {

	@Autowired
	private CompanySsoProvider companySsoProvider;

	@Autowired
	private StaticBasicParserPool parserPool;

	@Autowired
	private ExtendedMetadata extendedMetadata;

	private String baseEntityId;

	/**
	 * Default constructor.
	 *
	 * @param generator generator
	 */
	public CustomMetadataGeneratorFilter(MetadataGenerator generator) {
		super(generator);
	}

	@Override
	protected void processMetadataInitialization(HttpServletRequest request) throws ServletException {
		// In case the hosted SP metadata weren't initialized, let's do it now
		if (manager.getHostedSPName() == null) {
			synchronized (CustomMetadataGenerator.class) {
				if (manager.getHostedSPName() == null) {
					generateMetadata(request, null);
				}
			}
		}
		if (SAMLUtil.processFilter("/sso/saml", request)
				|| (displayFilter != null && SAMLUtil.processFilter(displayFilter.getFilterProcessesUrl(), request))) {
			String companyId = SSOUtils.populateCompanyId(request);
			String key = request.getParameter("ssoKey");
			CompanySsoConfigResponse configResponse = companySsoProvider.getSsoConfigResponse(companyId);
			LoginConfig loginConfig = SSOUtils.getLoginConfig(configResponse, key);
			if (!(loginConfig instanceof SAMLConfig)) {
				loginConfig = SSOUtils.getLoginConfig(configResponse, SsoType.SAML);
			}
			SSOUtils.setCurrentLoginConfig(loginConfig);
			doGenerateSamlMeta(request, loginConfig);
		}
	}

	private void doGenerateSamlMeta(HttpServletRequest request, LoginConfig loginConfig) throws ServletException {
		if (loginConfig instanceof SAMLConfig) {
			SAMLConfig samlConfig = (SAMLConfig) loginConfig;
			String newEntityId = StringUtils.join(getBaseEntityId(), "/", loginConfig.getCompanyCode());
			if (getMetadataProvider(newEntityId) == null) {
				synchronized (CustomMetadataGenerator.class) {
					if (getMetadataProvider(newEntityId) == null) {
						try {
							boolean signEnabled = BooleanUtils.toBoolean(samlConfig.getVerifySignatureNeeded());
							generator.setRequestSigned(signEnabled);
							generator.setWantAssertionSigned(signEnabled);
							generator.setEntityId(newEntityId);
							generateMetadata(request, loginConfig);
						} finally {
							generator.setEntityId(getBaseEntityId());
						}
					}
				}
			}
			calcIdpMeta(request, samlConfig);
		}
	}

	private void calcIdpMeta(HttpServletRequest request, SAMLConfig samlConfig) {
		try {
			CompanyMetadataProvider companyMetadataProvider = new CompanyMetadataProvider(samlConfig);
			String newPeerEntityId = companyMetadataProvider.getMetadataIdentifier();
			if (StringUtils.isNotBlank(samlConfig.getEntityId())) {
				newPeerEntityId = (samlConfig).getEntityId();
				companyMetadataProvider.setMetadataIdentifier(newPeerEntityId);
			}
			if (BooleanUtils.toBoolean(request.getParameter("refresh"))) {
				removeMetadataProvider(newPeerEntityId);
			}
			if (getMetadataProvider(newPeerEntityId) == null) {
				synchronized (CompanyMetadataProvider.class) {
					if (getMetadataProvider(newPeerEntityId) == null) {
						addOrReplaceIDPMetadataProvider(companyMetadataProvider, newPeerEntityId, samlConfig);
					}
				}
			}
		} catch (MetadataProviderException e) {
			logger.error("获取idp的metadata错误", e);
		}
	}

	private void addOrReplaceIDPMetadataProvider(CompanyMetadataProvider companyMetadataProvider, String newPeerEntityId, SAMLConfig loginConfig) throws MetadataProviderException {
		companyMetadataProvider.setParserPool(parserPool);
		ExtendedMetadata extendedMetadataClone = this.extendedMetadata.clone();
		extendedMetadataClone.setSigningKey(loginConfig.getKeyAlias());
		extendedMetadataClone.setSignMetadata(true);
		ExtendedMetadataDelegate extendedMetadataDelegate = new ExtendedMetadataDelegate(companyMetadataProvider, extendedMetadataClone);
		extendedMetadataDelegate.setMetadataTrustCheck(false);
		addOrReplaceMetadataProvider(extendedMetadataDelegate, newPeerEntityId);
		manager.refreshMetadata(); // 添加进去就把数据刷新
		EntityDescriptor entityDescriptor = null;
		if (companyMetadataProvider.getMetadata() instanceof EntitiesDescriptor) {
			EntitiesDescriptor entitiesDescriptor = (EntitiesDescriptor) companyMetadataProvider.getMetadata();
			if (CollectionUtils.isNotEmpty(entitiesDescriptor.getEntityDescriptors())) {
				for (EntityDescriptor descriptor : entitiesDescriptor.getEntityDescriptors()) {
					IDPSSODescriptor idpssoDescriptor = descriptor.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
					if (idpssoDescriptor != null) {
						entityDescriptor = descriptor;
						break;
					}
				}
			}
		} else if (companyMetadataProvider.getMetadata() instanceof EntityDescriptor) {
			entityDescriptor = (EntityDescriptor) companyMetadataProvider.getMetadata();
		}
		if (entityDescriptor != null && !StringUtils.equals(newPeerEntityId, entityDescriptor.getEntityID())) {
			SSOUtils.putSamlEntityId(newPeerEntityId, entityDescriptor.getEntityID());
		}
	}

	private void generateMetadata(HttpServletRequest request, LoginConfig loginConfig) throws ServletException {
		try {
			log.info("No default metadata configured, generating with default values, please pre-configure metadata for production use");
			// Defaults
			String alias = null;
			String baseURL = getDefaultBaseURL(request);
			// Use default baseURL if not set
			if (generator.getEntityBaseURL() == null) {
				log.warn("Generated default entity base URL {} based on values in the first server request. Please set property entityBaseURL on MetadataGenerator bean to fixate the value.", baseURL);
				generator.setEntityBaseURL(baseURL);
			} else {
				baseURL = generator.getEntityBaseURL();
			}
			// Use default entityID if not set
			if (generator.getEntityId() == null) {
				generator.setEntityId(getDefaultEntityID(baseURL, alias));
			}
			EntityDescriptor descriptor = generator.generateMetadata();
			ExtendedMetadata generateExtendedMetadata = generator.generateExtendedMetadata();
			log.info("Created default metadata for system with entityID: {}", descriptor.getEntityID());
			MetadataMemoryProvider memoryProvider = new MetadataMemoryProvider(descriptor);
			memoryProvider.initialize();
			MetadataProvider metadataProvider = new ExtendedMetadataDelegate(memoryProvider, generateExtendedMetadata);
			addOrReplaceMetadataProvider(metadataProvider, descriptor.getEntityID());
			if (loginConfig == null) {
				manager.setHostedSPName(descriptor.getEntityID());
			}
			manager.refreshMetadata();
		} catch (MetadataProviderException e) {
			throw new ServletException("Error generating system metadata", e);
		}
	}

	private void addOrReplaceMetadataProvider(MetadataProvider metadataProvider, String entityId) throws MetadataProviderException {
		removeMetadataProvider(entityId);
		manager.addMetadataProvider(metadataProvider);
	}

	private void removeMetadataProvider(String entityId) {
		ExtendedMetadataDelegate existProvider = getMetadataProvider(entityId);
		if (existProvider != null) {
			manager.removeMetadataProvider(existProvider);
		}
	}

	private ExtendedMetadataDelegate getMetadataProvider(String entityId) {
		ExtendedMetadataDelegate existProvider = null;
		for (ExtendedMetadataDelegate provider : manager.getAvailableProviders()) {
			try {
				EntityDescriptor entityDescriptor = provider.getEntityDescriptor(entityId);
				if (entityDescriptor != null) {
					existProvider = provider;
				}
			} catch (Exception e) {
				logger.error("获取Provider错误", e);
			}
		}
		return existProvider;
	}

	private String getBaseEntityId() {
		return baseEntityId;
	}

	public void setBaseEntityId(String baseEntityId) {
		this.baseEntityId = baseEntityId;
	}
}

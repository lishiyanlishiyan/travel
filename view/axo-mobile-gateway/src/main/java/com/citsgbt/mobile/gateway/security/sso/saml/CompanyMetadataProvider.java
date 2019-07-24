package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsgbt.mobile.gateway.security.sso.utils.KeyUtil;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.*;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityConfiguration;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorManager;
import org.opensaml.xml.security.keyinfo.NamedKeyInfoGeneratorManager;
import org.opensaml.xml.signature.KeyInfo;
import org.slf4j.Logger;
import org.springframework.xml.transform.StringResult;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 由于没有从公司对接服务商获取到metadata.xml，自己生成一个
 *
 * @author gary.fu
 */
class CompanyMetadataProvider extends FilesystemMetadataProvider {

	private static final Logger logger = getLogger(CompanyMetadataProvider.class);

	private SAMLConfig samlConfig;

	private String metadataIdentifier;

	public CompanyMetadataProvider(File metadata, SAMLConfig samlConfig) throws MetadataProviderException {
		super(metadata);
		this.samlConfig = samlConfig;
	}

	public CompanyMetadataProvider(SAMLConfig samlConfig) throws MetadataProviderException {
		this(null, samlConfig);
		File file = null;
		if (StringUtils.isNotBlank(samlConfig.getMetadataPath())) {
			file = new File(samlConfig.getMetadataPath());
		}
		setMetadataFile(file);
		metadataIdentifier = SSOUtils.getPeerEntityId(samlConfig, null);
	}

	@Override
	protected String getMetadataIdentifier() {
		return metadataIdentifier;
	}

	@Override
	protected byte[] fetchMetadata() throws MetadataProviderException {
		if (StringUtils.isNotBlank(samlConfig.getMetadataPath())) {
			return super.fetchMetadata();
		} else {
			try {
				String metaStr = generateMetadata();
				return metaStr.getBytes(StandardCharsets.UTF_8);
			} catch (Exception e) {
				logger.error("生成metadata失败", e);
			}
		}
		return new byte[0];
	}

	/**
	 * 客户没有提供metadata.xml，根据配置、证书生成一个metadata
	 *
	 * @return
	 * @throws Exception
	 */
    private String generateMetadata() throws NoSuchFieldException, IllegalAccessException, CertificateException, IOException, ParserConfigurationException, MarshallingException, TransformerException {
		String ssoUrl = samlConfig.getDestination();
		EntityDescriptor spEntityDescriptor = buildSAMLObjectWithDefaultName(EntityDescriptor.class);
		spEntityDescriptor.setEntityID(getMetadataIdentifier());
		spEntityDescriptor.setID(getMetadataIdentifier());
		IDPSSODescriptor idpssoDescriptor = buildSAMLObjectWithDefaultName(IDPSSODescriptor.class);
		idpssoDescriptor.setWantAuthnRequestsSigned(BooleanUtils.toBoolean(samlConfig.getVerifySignatureNeeded()));
		if (StringUtils.isNotBlank(samlConfig.getCertPath())) {
			File certFile = new File(samlConfig.getCertPath());
			if (certFile.exists()) {
				// 公钥 ENCRYPTION
				Certificate certificate = KeyUtil.getCertificate(samlConfig.getCertPath(), samlConfig.getCertType());
				Credential goodCredential = SecurityHelper.getSimpleCredential((X509Certificate) certificate, null);
				KeyDescriptor encKeyDescriptor = buildSAMLObjectWithDefaultName(KeyDescriptor.class);
				encKeyDescriptor.setUse(UsageType.ENCRYPTION); //Set usage
				// Generating key info. The element will contain the public key. The key is used to by the IDP to encrypt data
				try {
					encKeyDescriptor.setKeyInfo(getKeyInfo(goodCredential));
				} catch (SecurityException e) {
					logger.error(e.getMessage(), e);
				}
				// 公钥 SIGNING
				idpssoDescriptor.getKeyDescriptors().add(encKeyDescriptor);
				KeyDescriptor signKeyDescriptor = buildSAMLObjectWithDefaultName(KeyDescriptor.class);
				signKeyDescriptor.setUse(UsageType.SIGNING);  //Set usage
				// Generating key info. The element will contain the public key. The key is used to by the IDP to verify signatures
				try {
					signKeyDescriptor.setKeyInfo(getKeyInfo(goodCredential));
				} catch (SecurityException e) {
					logger.error(e.getMessage(), e);
				}
				idpssoDescriptor.getKeyDescriptors().add(signKeyDescriptor);
			}
		}

		// Request transient pseudonym
		NameIDFormat nameIDFormat = buildSAMLObjectWithDefaultName(NameIDFormat.class);
		nameIDFormat.setFormat(StringUtils.isBlank(samlConfig.getNameIDPolicyFormat())
				? "urn:oasis:names:tc:SAML:2.0:nameid-format:transient" : samlConfig.getNameIDPolicyFormat());
		idpssoDescriptor.getNameIDFormats().add(nameIDFormat);

		SingleSignOnService singleSignOnService = buildSAMLObjectWithDefaultName(SingleSignOnService.class);
		singleSignOnService.setBinding(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
		singleSignOnService.setLocation(ssoUrl);
		idpssoDescriptor.getSingleSignOnServices().add(singleSignOnService);

		SingleSignOnService singleSignOnServicePOST = buildSAMLObjectWithDefaultName(SingleSignOnService.class);
		singleSignOnServicePOST.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
		singleSignOnServicePOST.setLocation(ssoUrl);
		idpssoDescriptor.getSingleSignOnServices().add(singleSignOnServicePOST);

		SingleLogoutService singleLogoutService = buildSAMLObjectWithDefaultName(SingleLogoutService.class);
		singleLogoutService.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
		singleLogoutService.setLocation(samlConfig.getLogoutURL());
		idpssoDescriptor.getSingleLogoutServices().add(singleLogoutService);
		idpssoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

		spEntityDescriptor.getRoleDescriptors().add(idpssoDescriptor);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
		Marshaller out = Configuration.getMarshallerFactory().getMarshaller(spEntityDescriptor);
		out.marshall(spEntityDescriptor, document);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		StringResult streamResult = new StringResult();
		DOMSource source = new DOMSource(document);
		transformer.transform(source, streamResult);
		return streamResult.toString();
	}


	private static <T> T buildSAMLObjectWithDefaultName(final Class<T> clazz) throws NoSuchFieldException, IllegalAccessException {
		XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
		QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
		return (T) builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
	}

	private KeyInfo getKeyInfo(Credential credential) throws SecurityException {
		SecurityConfiguration secConfiguration = Configuration.getGlobalSecurityConfiguration();
		NamedKeyInfoGeneratorManager namedKeyInfoGeneratorManager = secConfiguration.getKeyInfoGeneratorManager();
		KeyInfoGeneratorManager keyInfoGeneratorManager = namedKeyInfoGeneratorManager.getDefaultManager();
		KeyInfoGeneratorFactory factory = keyInfoGeneratorManager.getFactory(credential);
		KeyInfoGenerator generator = factory.newInstance();
		return generator.generate(credential);
	}

	public SAMLConfig getSamlConfig() {
		return samlConfig;
	}

	public void setSamlConfig(SAMLConfig samlConfig) {
		this.samlConfig = samlConfig;
	}

	public void setMetadataIdentifier(String metadataIdentifier) {
		this.metadataIdentifier = metadataIdentifier;
	}

}

package com.citsgbt.mobile.gateway.security.sso.vo;

public class SAMLConfig extends LoginConfig {
	private String type;
	private String keyPath;
	private String keyType;
	private String keyAlg;
	private String keyPassword;
	private String keyAlias;
	private String certPath;
	private String certType;
	private String issuer;
	private String providerName;
	private String assertionConsumerServiceURL;
	private String destination;
	private String userIDAttrName;
	private String companyIDAttrName;
	private String verifySignatureNeeded;
	private String relayState;
	private String nameIDPolicyFormat;
	private String metadataPath;
	private String entityId;

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	public String getKeyPath() {
		return keyPath;
	}

	public void setKeyPath(String keyPath) {
		this.keyPath = keyPath;
	}

	public String getKeyType() {
		return keyType;
	}

	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}

	public String getKeyAlg() {
		return keyAlg;
	}

	public void setKeyAlg(String keyAlg) {
		this.keyAlg = keyAlg;
	}

	public String getKeyPassword() {
		return keyPassword;
	}

	public void setKeyPassword(String keyPassword) {
		this.keyPassword = keyPassword;
	}

	public String getKeyAlias() {
		return keyAlias;
	}

	public void setKeyAlias(String keyAlias) {
		this.keyAlias = keyAlias;
	}

	public String getCertPath() {
		return certPath;
	}

	public void setCertPath(String certPath) {
		this.certPath = certPath;
	}

	public String getCertType() {
		return certType;
	}

	public void setCertType(String certType) {
		this.certType = certType;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getAssertionConsumerServiceURL() {
		return assertionConsumerServiceURL;
	}

	public void setAssertionConsumerServiceURL(String assertionConsumerServiceURL) {
		this.assertionConsumerServiceURL = assertionConsumerServiceURL;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getUserIDAttrName() {
		return userIDAttrName;
	}

	public void setUserIDAttrName(String userIDAttrName) {
		this.userIDAttrName = userIDAttrName;
	}

	public String getCompanyIDAttrName() {
		return companyIDAttrName;
	}

	public void setCompanyIDAttrName(String companyIDAttrName) {
		this.companyIDAttrName = companyIDAttrName;
	}


	public String getRelayState() {
		return relayState;
	}

	public void setRelayState(String relayState) {
		this.relayState = relayState;
	}

	public String getVerifySignatureNeeded() {
		return verifySignatureNeeded;
	}

	public void setVerifySignatureNeeded(String verifySignatureNeeded) {
		this.verifySignatureNeeded = verifySignatureNeeded;
	}

	public String getNameIDPolicyFormat() {
		return nameIDPolicyFormat;
	}

	public void setNameIDPolicyFormat(String nameIDPolicyFormat) {
		this.nameIDPolicyFormat = nameIDPolicyFormat;
	}

	public String getMetadataPath() {
		return metadataPath;
	}

	public void setMetadataPath(String metadataPath) {
		this.metadataPath = metadataPath;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}
}

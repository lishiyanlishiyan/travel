package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.security.saml.SAMLConstants;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.trust.MetadataCredentialResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class CustomSAMLContextProviderImpl extends SAMLContextProviderImpl {

	@Override
	public SAMLMessageContext getLocalEntity(HttpServletRequest request, HttpServletResponse response) throws MetadataProviderException {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig != null) {
			request.setAttribute(SAMLConstants.LOCAL_ENTITY_ID, SSOUtils.getEntityId(loginConfig, metadata.getHostedSPName()));
			return super.getLocalEntity(request, response);
		}
		throw new MetadataProviderException("Metadata not exists.");
	}

	@Override
	public SAMLMessageContext getLocalAndPeerEntity(HttpServletRequest request, HttpServletResponse response) throws MetadataProviderException {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig != null) {
			request.setAttribute(SAMLConstants.LOCAL_ENTITY_ID, SSOUtils.getEntityId(loginConfig, metadata.getHostedSPName()));
			request.setAttribute(SAMLConstants.PEER_ENTITY_ID, SSOUtils.getPeerEntityId(loginConfig, metadata.getHostedSPName()));
			return super.getLocalAndPeerEntity(request, response);
		}
		throw new MetadataProviderException("Metadata not exists.");
	}

	@Override
	public void afterPropertiesSet() throws ServletException {
		super.afterPropertiesSet();
		if (this.metadataResolver instanceof MetadataCredentialResolver) {
			((MetadataCredentialResolver) this.metadataResolver).setUseExtendedMetadata(false);
		}
	}
}

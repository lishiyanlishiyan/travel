package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 按照xml方式显示saml meta
 *
 * @author gary.fu
 */
class CustomMetadataDisplayFilter extends MetadataDisplayFilter {

	@Override
	protected void processMetadataDisplay(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		try {
			String companyId = SSOUtils.populateCompanyId(request);
			String idp = request.getParameter("idpFlag");
			String entityId;
			if (StringUtils.isNotBlank(companyId) && BooleanUtils.toBoolean(idp)) {
				SAMLMessageContext peerContext = contextProvider.getLocalAndPeerEntity(request, response);
				entityId = peerContext.getPeerEntityId();
			} else {
				SAMLMessageContext context = contextProvider.getLocalEntity(request, response);
				entityId = context.getLocalEntityId();
			}
			response.setContentType("application/xml"); // SAML_Meta, 4.1.1 - line 1235
			displayMetadata(entityId, response.getWriter());
		} catch (MetadataProviderException e) {
			throw new ServletException("Error initializing metadata", e);
		}
	}

}

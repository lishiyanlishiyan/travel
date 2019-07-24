package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.common.SAMLException;
import org.opensaml.common.binding.decoding.BasicURLComparator;
import org.opensaml.common.binding.decoding.URIComparator;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.transport.InTransport;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.util.DatatypeHelper;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml.SAMLAuthenticationToken;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.util.SAMLUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * spring中SAMLProcessingFilter处理request url去掉了参数，不兼容老axo的多个公司saml地址<br>
 * 修改保留url中的参数
 *
 * @author gary.fu
 */
class CustomSAMLProcessingFilter extends SAMLProcessingFilter {

	private final URIComparator uriComparator = new BasicURLComparator();

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
		try {

			logger.debug("Attempting SAML2 authentication using profile {}", getProfileName());
			SAMLMessageContext context = contextProvider.getLocalEntity(request, response);
			processor.retrieveMessage(context);
			// Override set values
			context.setCommunicationProfileId(getProfileName());
			context.setLocalEntityEndpoint(getEndpoint(context));

			SAMLAuthenticationToken token = new SAMLAuthenticationToken(context);
			return getAuthenticationManager().authenticate(token);

		} catch (SAMLException | SecurityException e) {
			throw new AuthenticationServiceException("Incoming SAML message is invalid", e);
		} catch (MetadataProviderException e) {
			throw new AuthenticationServiceException("Error determining metadata contracts", e);
		} catch (MessageDecodingException e) {
			throw new AuthenticationServiceException("Error decoding incoming SAML message", e);
		}

	}

	private Endpoint getEndpoint(SAMLMessageContext context) throws SAMLException {
		return getEndpoint(context.getLocalEntityRoleMetadata().getEndpoints(), context.getInboundSAMLBinding(), context.getInboundMessageTransport());
	}

	/**
	 * 处理获取的url中没有参数问题
	 *
	 * @param endpoints
	 * @param messageBinding
	 * @param inTransport
	 * @param <T>
	 * @return
	 * @throws SAMLException
	 * @see SAMLUtil#getEndpoint(List, String, InTransport)
	 */
	private <T extends Endpoint> T getEndpoint(List<T> endpoints, String messageBinding, InTransport inTransport) throws SAMLException {
		HttpServletRequest httpRequest = ((HttpServletRequestAdapter) inTransport).getWrappedRequest();
		String requestURL = DatatypeHelper.safeTrimOrNullString(HttpRequestUtils.getRequestUrl(httpRequest));
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof SAMLConfig && StringUtils.isNotBlank(((SAMLConfig) loginConfig).getAssertionConsumerServiceURL())) {
			requestURL = ((SAMLConfig) loginConfig).getAssertionConsumerServiceURL();
		}
		for (T endpoint : endpoints) {
			String binding = SAMLUtil.getBindingForEndpoint(endpoint);
			// Check that destination and binding matches
			if (binding.equals(messageBinding)) {
				if (endpoint.getLocation() != null && uriComparator.compare(endpoint.getLocation(), requestURL)) {
					logger.debug("Found endpoint {} for request URL {} based on location attribute in metadata", endpoint, requestURL);
					return endpoint;
				} else if (endpoint.getResponseLocation() != null && uriComparator.compare(endpoint.getResponseLocation(), requestURL)) {
					logger.debug("Found endpoint {} for request URL {} based on response location attribute in metadata", endpoint, requestURL);
					return endpoint;
				}
			}
		}
		throw new SAMLException("Endpoint with message binding " + messageBinding + " and URL " + requestURL + " wasn't found in local metadata");
	}

}

package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.gateway.security.sso.SsoLoginResultVo;
import com.citsgbt.mobile.gateway.security.sso.utils.KeyUtil;
import com.citsgbt.mobile.gateway.security.sso.utils.Keys;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;
import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLException;
import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.Endpoint;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.SignatureImpl;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.storage.SAMLMessageStorage;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.saml.util.SAMLUtil.isDateTimeSkewValid;

/**
 * 处理SAMLResponse
 *
 * @author gary.fu
 */
class CustomWebSSOProfileConsumerImpl extends WebSSOProfileConsumerImpl {

	/**
	 * 复制super.processAuthenticationResponse();
	 *
	 * @param context
	 * @return
	 * @throws SAMLException
	 * @throws SecurityException
	 * @throws ValidationException
	 * @throws DecryptionException
	 */
	@Override
	public SAMLCredential processAuthenticationResponse(SAMLMessageContext context) throws SAMLException, SecurityException, ValidationException, DecryptionException {
		AuthnRequest request = null;
		SAMLObject message = context.getInboundSAMLMessage();

		// Verify type
		if (!(message instanceof Response)) {
			throw new SAMLException("Message is not of a Response object type");
		}
		Response response = (Response) message;
		// Verify status
		String statusCode = response.getStatus().getStatusCode().getValue();
		if (!StatusCode.SUCCESS_URI.equals(statusCode)) {
			StatusMessage statusMessage = response.getStatus().getStatusMessage();
			String statusMessageText = null;
			if (statusMessage != null) {
				statusMessageText = statusMessage.getMessage();
			}
			throw new SAMLException("Response has invalid status code " + statusCode + ", status message is " + statusMessageText);
		}

		// Verify signature of the response if present, unless already verified in binding
		if (response.getSignature() != null && !context.isInboundSAMLMessageAuthenticated()) {
			log.debug("Verifying Response signature");
			verifySignature(response.getSignature(), context.getPeerEntityId(), context.getLocalTrustEngine());
			context.setInboundSAMLMessageAuthenticated(true);
		}

		// Verify issue time
		DateTime time = response.getIssueInstant();
		if (!isDateTimeSkewValid(getResponseSkew(), time)) {
			throw new SAMLException("Response issue time is either too old or with date in the future, skew " + getResponseSkew() + ", time " + time);
		}

		// Reject unsolicited messages when disabled
		if (!context.getPeerExtendedMetadata().isSupportUnsolicitedResponse() && response.getInResponseTo() == null) {
			throw new SAMLException("Reception of Unsolicited Response messages (without InResponseToField) is disabled");
		}

		// Verify response to field if present, set request if correct
		SAMLMessageStorage messageStorage = context.getMessageStorage();
		if (messageStorage != null && response.getInResponseTo() != null) {
			XMLObject xmlObject = messageStorage.retrieveMessage(response.getInResponseTo());
			if (xmlObject == null) {
				throw new SAMLException("InResponseToField of the Response doesn't correspond to sent message " + response.getInResponseTo());
			} else if (xmlObject instanceof AuthnRequest) {
				request = (AuthnRequest) xmlObject;
			} else {
				throw new SAMLException("Sent request was of different type than the expected AuthnRequest " + response.getInResponseTo());
			}
		}

		// Verify that message was received at the expected endpoint
		verifyEndpoint(context.getLocalEntityEndpoint(), response.getDestination());

		// Verify endpoint requested in the original request
		if (request != null) {
			AssertionConsumerService assertionConsumerService = (AssertionConsumerService) context.getLocalEntityEndpoint();
			if (request.getAssertionConsumerServiceIndex() != null) {
				if (!request.getAssertionConsumerServiceIndex().equals(assertionConsumerService.getIndex())) {
					log.info("Response was received at a different endpoint index than was requested");
				}
			} else {
				String requestedResponseURL = request.getAssertionConsumerServiceURL();
				String requestedBinding = request.getProtocolBinding();
				if (requestedResponseURL != null) {
					String responseLocation;
					if (assertionConsumerService.getResponseLocation() != null) {
						responseLocation = assertionConsumerService.getResponseLocation();
					} else {
						responseLocation = assertionConsumerService.getLocation();
					}
					if (!requestedResponseURL.equals(responseLocation)) {
						log.info("Response was received at a different endpoint URL {} than was requested {}", responseLocation, requestedResponseURL);
					}
				}
				if (requestedBinding != null && !requestedBinding.equals(context.getInboundSAMLBinding())) {
					log.info("Response was received using a different binding {} than was requested {}", context.getInboundSAMLBinding(), requestedBinding);
				}
			}
		}

		// Verify issuer
		if (response.getIssuer() != null) {
			log.debug("Verifying issuer of the Response");
			Issuer issuer = response.getIssuer();
			verifyIssuer(issuer, context);
		}

		Assertion subjectAssertion = null;
		List<Attribute> attributes = new ArrayList<>();
		List<Assertion> assertionList = response.getAssertions();

		// Decrypt assertions
		if (!response.getEncryptedAssertions().isEmpty()) {
			assertionList = new ArrayList<>(response.getAssertions().size() + response.getEncryptedAssertions().size());
			assertionList.addAll(response.getAssertions());
			List<EncryptedAssertion> encryptedAssertionList = response.getEncryptedAssertions();
			for (EncryptedAssertion ea : encryptedAssertionList) {
				try {
					Assert.notNull(context.getLocalDecrypter(), "Can't decrypt Assertion, no decrypter is set in the context");
					log.debug("Decrypting assertion");
					Assertion decryptedAssertion = context.getLocalDecrypter().decrypt(ea);
					assertionList.add(decryptedAssertion);
				} catch (DecryptionException e) {
					log.debug("Decryption of received assertion failed, assertion will be skipped", e);
				}
			}
		}

		Exception lastError = null;

		// Find the assertion to be used for session creation and verify
		for (Assertion assertion : assertionList) {
			if (!assertion.getAuthnStatements().isEmpty()) {
				try {
					// Verify that the assertion is valid
					verifyAssertion(assertion, request, context);
					subjectAssertion = assertion;
					log.debug("Validation of authentication statement in assertion {} was successful", assertion.getID());
					break;
				} catch (Exception e) {
					log.debug("Validation of authentication statement in assertion failed, skipping", e);
					lastError = e;
				}
			} else {
				log.debug("Assertion {} did not contain any authentication statements, skipping", assertion.getID());
			}
		}

		// Make sure that at least one assertion contains authentication statement and subject with bearer confirmation
		if (subjectAssertion == null) {
			throw new SAMLException("Response doesn't have any valid assertion which would pass subject validation", lastError);
		}

		// Process attributes from assertions
		for (Assertion assertion : assertionList) {
			if (assertion == subjectAssertion || isIncludeAllAttributes()) {
				for (AttributeStatement attStatement : assertion.getAttributeStatements()) {
					for (Attribute att : attStatement.getAttributes()) {
						log.debug("Including attribute {} from assertion {}", att.getName(), assertion.getID());
						attributes.add(att);
					}
					for (EncryptedAttribute att : attStatement.getEncryptedAttributes()) {
						Assert.notNull(context.getLocalDecrypter(), "Can't decrypt Attribute, no decrypter is set in the context");
						Attribute decryptedAttribute = context.getLocalDecrypter().decrypt(att);
						log.debug("Including decrypted attribute {} from assertion {}", decryptedAttribute.getName(), assertion.getID());
						attributes.add(decryptedAttribute);
					}
				}
			}
		}
		NameID nameId = getNameID(context, attributes);
		// Populate custom data, if any
		Serializable additionalData = processAdditionalData(context);

		// Release extra DOM data which might get otherwise stored in session
		if (isReleaseDOM()) {
			subjectAssertion.releaseDOM();
			subjectAssertion.releaseChildrenDOM(true);
		}

		// Create the credential
		return new SAMLCredential(nameId, subjectAssertion, context.getPeerEntityMetadata().getEntityID(), context.getRelayState(), attributes, context.getLocalEntityId(), additionalData);

	}

	/**
	 * 老axo中获取用户信息除了从nameId获取，还要从attribute中获取，添加从attribute中获取用户名支持<br>
	 * 优先从attribute中获取用户名
	 *
	 * @param context
	 * @param attributes
	 * @return
	 * @throws SAMLException
	 */
	private NameID getNameID(SAMLMessageContext context, List<Attribute> attributes) throws SAMLException {
		NameID nameId = (NameID) context.getSubjectNameIdentifier();
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof SAMLConfig) {
			SAMLConfig samlConfig = (SAMLConfig) loginConfig;
			if (StringUtils.isNotBlank(samlConfig.getUserIDAttrName())) { // 从Attribute中获取优先
				for (Attribute attribute : attributes) {
					if (StringUtils.equalsIgnoreCase(attribute.getName(), ((SAMLConfig) loginConfig).getUserIDAttrName())
							|| StringUtils.equalsIgnoreCase(attribute.getFriendlyName(), ((SAMLConfig) loginConfig).getUserIDAttrName())) {
						nameId = new NameIDBuilder().buildObject();
						nameId.setValue(attribute.getAttributeValues().get(0).getDOM().getTextContent());
						break;
					}
				}
			}
		}
		if (nameId == null) {
			throw new SAMLException("NameID element must be present as part of the Subject in the Response message, please enable it in the IDP configuration");
		}
		HttpServletRequest request = HttpRequestUtils.getCurrentRequest();
		if (request != null) {
			SsoLoginResultVo loginResultVo = (SsoLoginResultVo) request.getAttribute(Keys.SSO_LOGIN_RESULT_KEY);
			if (loginResultVo != null) {
				loginResultVo.setLoginVo(new LoginVo(loginConfig.getCompanyCode(), nameId.getValue()));
			}
		}
		return nameId;
	}

	@Override
	protected void verifyIssuer(Issuer issuer, SAMLMessageContext context) throws SAMLException {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof SAMLConfig) {
			SAMLConfig samlConfig = (SAMLConfig) loginConfig;
			if (StringUtils.isNotBlank(samlConfig.getEntityId()) || StringUtils.isNotBlank(samlConfig.getMetadataPath())) { // 配置了idp的entityId的话验证Issuer
				super.verifyIssuer(issuer, context);
			}
		}
	}

	@Override
	protected void verifyAuthenticationStatement(AuthnStatement auth, RequestedAuthnContext requestedAuthnContext, SAMLMessageContext context) {
		// 去掉AuthnStatement时间校验
		super.verifyAuthnContext(requestedAuthnContext, auth.getAuthnContext(), context);
	}

	@Override
	protected void verifyAudience(SAMLMessageContext context, List<AudienceRestriction> audienceRestrictions) throws SAMLException {
		// Multiple AudienceRestrictions form a logical "AND" (SAML-core, 922-925)
		audience:
		for (AudienceRestriction rest : audienceRestrictions) {
			if (rest.getAudiences().isEmpty()) {
				throw new SAMLException("No audit audience specified for the assertion");
			}
			for (Audience aud : rest.getAudiences()) {
				// Multiple Audiences within one AudienceRestriction form a logical "OR" (SAML-core, 922-925)
				if (getLocalEntityIssuer(context.getLocalEntityId()).equalsIgnoreCase(aud.getAudienceURI())) {
					continue audience;
				}
			}
			throw new SAMLException("Local entity is not the intended audience of the assertion in at least " +
					"one AudienceRestriction");
		}
	}

	@Override
	protected void verifySignature(Signature signature, String idpEntityId, SignatureTrustEngine trustEngine) throws SecurityException, ValidationException {
		try {
			super.verifySignature(signature, idpEntityId, trustEngine);
		} catch (ValidationException e) {
			log.error("本地Signature验证失败", e);
			if (!verifySignature((SignatureImpl) signature)) {
				throw e;
			}
		}
	}

	/**
	 * 使用请求中的PublicKey验证
	 *
	 * @param signature
	 * @throws ValidationException
	 */
	private boolean verifySignature(SignatureImpl signature) throws ValidationException {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof SAMLConfig) {
			SAMLConfig samlConfig = (SAMLConfig) loginConfig;
			XMLSignature xmlSignature = signature.getXMLSignature();
			if (xmlSignature.getKeyInfo() != null) {
				try {
					X509Certificate cert = xmlSignature.getKeyInfo().getX509Certificate();
					PublicKey pk;
					if (cert != null) {
						pk = cert.getPublicKey();
					} else {
						pk = xmlSignature.getKeyInfo().getPublicKey();
						if (pk == null) {
							pk = KeyUtil.getPublicKeyFromCert(samlConfig);
						}
					}
					return xmlSignature.checkSignatureValue(pk);
				} catch (Exception e) {
					throw new ValidationException("Signature验证失败", e);
				}
			}
		}
		return false;
	}

	@Override
	protected void verifyEndpoint(Endpoint endpoint, String destination) {
		// noop 不验证Endpoint
	}

	@Override
	protected void verifySubject(Subject subject, AuthnRequest request, SAMLMessageContext context) throws SAMLException, DecryptionException {
		for (SubjectConfirmation confirmation : subject.getSubjectConfirmations()) {
			if (SubjectConfirmation.METHOD_BEARER.equals(confirmation.getMethod())) {
				log.debug("Processing Bearer subject confirmation");
				SubjectConfirmationData data = confirmation.getSubjectConfirmationData();
				// Bearer must have confirmation SAML-profiles-2.0-os 554
				if (data == null) {
					log.debug("Bearer SubjectConfirmation invalidated by missing confirmation data");
					continue;
				}
				// Validate in response to
				if (request != null) {
					if (data.getInResponseTo() == null) {
						log.debug("Bearer SubjectConfirmation invalidated by missing inResponseTo field");
						continue;
					} else {
						if (!data.getInResponseTo().equals(request.getID())) {
							log.debug("Bearer SubjectConfirmation invalidated by invalid in response to");
							continue;
						}
					}
				}
				// Was the subject confirmed by this confirmation data? If so let's store the subject in the context.
				NameID nameID;
				if (subject.getEncryptedID() != null) {
					Assert.notNull(context.getLocalDecrypter(), "Can't decrypt NameID, no decrypter is set in the context");
					nameID = (NameID) context.getLocalDecrypter().decrypt(subject.getEncryptedID());
				} else {
					nameID = subject.getNameID();
				}
				context.setSubjectNameIdentifier(nameID);
				return;
			}
		}
		throw new SAMLException("Assertion invalidated by subject confirmation - can't be confirmed by the bearer method");
	}

	@Override
	protected void verifyAssertionConditions(Conditions conditions, SAMLMessageContext context, boolean audienceRequired) throws SAMLException {
		if (conditions == null || conditions.getAudienceRestrictions().isEmpty()) { // 没有值不校验
			return;
		}
		super.verifyAssertionConditions(conditions, context, audienceRequired);
	}

	/**
	 * 替换LocalEntity为issuer
	 *
	 * @param localEntityId
	 * @return
	 */
	private String getLocalEntityIssuer(String localEntityId) {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof SAMLConfig) {
			SAMLConfig samlConfig = (SAMLConfig) loginConfig;
			if (StringUtils.isNotBlank(samlConfig.getIssuer())) { // 获取Issuer
				return samlConfig.getIssuer();
			}
		}
		return localEntityId;
	}
}

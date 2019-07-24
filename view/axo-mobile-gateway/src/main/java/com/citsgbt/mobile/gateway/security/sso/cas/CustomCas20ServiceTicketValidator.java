package com.citsgbt.mobile.gateway.security.sso.cas;

import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.proxy.Cas20ProxyRetriever;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.util.XmlUtils;
import org.jasig.cas.client.validation.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.net.URL;
import java.util.*;

import static com.citsgbt.mobile.gateway.security.sso.cas.CustomProxyTicketValidator.CAS_SERVICE_RESPONSE_KEY;

/**
 * 验证工具，为了获取日志信息, 很多方法都是final，不能覆写
 * <br>
 * 复制Cas20ServiceTicketValidator和AbstractCasProtocolUrlBasedTicketValidator代码
 *
 * @author gary.fu
 * @see Cas20ServiceTicketValidator
 * @see AbstractCasProtocolUrlBasedTicketValidator
 */
public class CustomCas20ServiceTicketValidator extends AbstractUrlBasedTicketValidator {

	/**
	 * The CAS 2.0 protocol proxy callback url.
	 */
	private String proxyCallbackUrl;

	/**
	 * The storage location of the proxy granting tickets.
	 */
	private ProxyGrantingTicketStorage proxyGrantingTicketStorage;

	/**
	 * Implementation of the proxy retriever.
	 */
	private ProxyRetriever proxyRetriever;

	/**
	 * Constructs an instance of the CAS 2.0 Service Ticket Validator with the supplied
	 * CAS server url prefix.
	 *
	 * @param casServerUrlPrefix the CAS Server URL prefix.
	 */
	public CustomCas20ServiceTicketValidator(final String casServerUrlPrefix) {
		super(casServerUrlPrefix);
		this.proxyRetriever = new Cas20ProxyRetriever(casServerUrlPrefix, getEncoding(), getURLConnectionFactory());
	}

	/**
	 * Adds the pgtUrl to the list of parameters to pass to the CAS server.
	 *
	 * @param urlParameters the Map containing the existing parameters to send to the server.
	 */
	@Override
	protected final void populateUrlAttributeMap(final Map<String, String> urlParameters) {
		urlParameters.put("pgtUrl", this.proxyCallbackUrl);
	}

	protected String getUrlSuffix() {
		return "serviceValidate";
	}

	protected Assertion parseResponseFromServer(final String response) throws TicketValidationException {
		final String error = XmlUtils.getTextForElement(response, "authenticationFailure");

		if (CommonUtils.isNotBlank(error)) {
			throw new TicketValidationException(error);
		}

		final String principal = XmlUtils.getTextForElement(response, "user");
		final String proxyGrantingTicketIou = XmlUtils.getTextForElement(response, "proxyGrantingTicket");

		final String proxyGrantingTicket;
		if (CommonUtils.isBlank(proxyGrantingTicketIou) || this.proxyGrantingTicketStorage == null) {
			proxyGrantingTicket = null;
		} else {
			proxyGrantingTicket = this.proxyGrantingTicketStorage.retrieve(proxyGrantingTicketIou);
		}

		if (CommonUtils.isEmpty(principal)) {
			throw new TicketValidationException("No principal was found in the response from the CAS server.");
		}

		final Assertion assertion;
		final Map<String, Object> attributes = extractCustomAttributes(response);
		if (CommonUtils.isNotBlank(proxyGrantingTicket)) {
			final AttributePrincipal attributePrincipal = new AttributePrincipalImpl(principal, attributes,
					proxyGrantingTicket, this.proxyRetriever);
			assertion = new AssertionImpl(attributePrincipal);
		} else {
			assertion = new AssertionImpl(new AttributePrincipalImpl(principal, attributes));
		}

		customParseResponse(response, assertion);

		return assertion;
	}

	/**
	 * Default attribute parsing of attributes that look like the following:
	 * &lt;CAS:attributes&gt;
	 * &lt;CAS:attribute1&gt;value&lt;/CAS:attribute1&gt;
	 * &lt;CAS:attribute2&gt;value&lt;/CAS:attribute2&gt;
	 * &lt;/CAS:attributes&gt;
	 * <p>
	 * <p>
	 * Attributes look like following also parsed correctly:
	 * &lt;CAS:attributes&gt;&lt;CAS:attribute1&gt;value&lt;/CAS:attribute1&gt;&lt;CAS:attribute2&gt;value&lt;/CAS:attribute2&gt;&lt;/CAS:attributes&gt;
	 * <p>
	 * <p>
	 * This code is here merely for sample/demonstration purposes for those wishing to modify the CAS2 protocol.  You'll
	 * probably want a more robust implementation or to use SAML 1.1
	 *
	 * @param xml the XML to parse.
	 * @return the map of attributes.
	 */
    private Map<String, Object> extractCustomAttributes(final String xml) {
		final SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
		} catch (ParserConfigurationException |SAXNotRecognizedException | SAXNotSupportedException  e) {
			logger.error("设置SAXParserFactory属性失败", e);
		}

		spf.setNamespaceAware(true);
		spf.setValidating(false);
		try {
			final SAXParser saxParser = spf.newSAXParser();
			final XMLReader xmlReader = saxParser.getXMLReader();
			final CustomCas20ServiceTicketValidator.CustomAttributeHandler handler = new CustomCas20ServiceTicketValidator.CustomAttributeHandler();
			xmlReader.setContentHandler(handler);
			xmlReader.parse(new InputSource(new StringReader(xml)));
			return handler.getAttributes();
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
			return Collections.emptyMap();
		}
	}

	/**
	 * Template method if additional custom parsing (such as Proxying) needs to be done.
	 *
	 * @param response  the original response from the CAS server.
	 * @param assertion the partially constructed assertion.
	 */
    private void customParseResponse(final String response, final Assertion assertion) {
		// nothing to do
	}

	public final void setProxyCallbackUrl(final String proxyCallbackUrl) {
		this.proxyCallbackUrl = proxyCallbackUrl;
	}

	public final void setProxyGrantingTicketStorage(final ProxyGrantingTicketStorage proxyGrantingTicketStorage) {
		this.proxyGrantingTicketStorage = proxyGrantingTicketStorage;
	}

	public final void setProxyRetriever(final ProxyRetriever proxyRetriever) {
		this.proxyRetriever = proxyRetriever;
	}

	protected final String getProxyCallbackUrl() {
		return this.proxyCallbackUrl;
	}

	protected final ProxyGrantingTicketStorage getProxyGrantingTicketStorage() {
		return this.proxyGrantingTicketStorage;
	}

	protected final ProxyRetriever getProxyRetriever() {
		return this.proxyRetriever;
	}

	private class CustomAttributeHandler extends DefaultHandler {

		private Map<String, Object> attributes;

		private boolean foundAttributes;

		private String currentAttribute;

		private StringBuilder value;

		@Override
		public void startDocument() {
			this.attributes = new HashMap<>();
		}

		@Override
		public void startElement(final String namespaceURI, final String localName, final String qName,
								 final Attributes attributes) {
			if ("attributes".equals(localName)) {
				this.foundAttributes = true;
			} else if (this.foundAttributes) {
				this.value = new StringBuilder();
				this.currentAttribute = localName;
			}
		}

		@Override
		public void characters(final char[] chars, final int start, final int length) {
			if (this.currentAttribute != null) {
				value.append(chars, start, length);
			}
		}

		@Override
		public void endElement(final String namespaceURI, final String localName, final String qName) {
			if ("attributes".equals(localName)) {
				this.foundAttributes = false;
				this.currentAttribute = null;
			} else if (this.foundAttributes) {
				final Object o = this.attributes.get(this.currentAttribute);

				if (o == null) {
					this.attributes.put(this.currentAttribute, this.value.toString());
				} else {
					final List<Object> items;
					if (o instanceof List) {
						items = (List<Object>) o;
					} else {
						items = new LinkedList<>();
						items.add(o);
						this.attributes.put(this.currentAttribute, items);
					}
					items.add(this.value.toString());
				}
			}
		}

		Map<String, Object> getAttributes() {
			return this.attributes;
		}
	}

	/**
	 * Retrieves the response from the server by opening a connection and merely reading the response.
	 */
	protected String retrieveResponseFromServer(final URL validationUrl, final String ticket) {
		String response = CommonUtils.getResponseFromServer(validationUrl, getURLConnectionFactory(), getEncoding());
		HttpServletRequest request = HttpRequestUtils.getCurrentRequest();
		request.setAttribute(CAS_SERVICE_RESPONSE_KEY, response);
		return response;
	}
}

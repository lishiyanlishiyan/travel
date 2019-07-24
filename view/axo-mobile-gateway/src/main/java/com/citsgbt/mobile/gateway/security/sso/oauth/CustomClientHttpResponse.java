package com.citsgbt.mobile.gateway.security.sso.oauth;

import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import org.apache.commons.collections.MapUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.AbstractClientHttpResponse;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

class CustomClientHttpResponse extends AbstractClientHttpResponse {

	private final HTTPResponse httpResponse;
	private final HttpHeaders headers;

	public CustomClientHttpResponse(HTTPResponse httpResponse) {
		Assert.notNull(httpResponse, "httpResponse cannot be null");
		this.httpResponse = httpResponse;
		this.headers = new HttpHeaders();
		Map<String, List<String>> headerMap = httpResponse.getHeaderMap();
		if (MapUtils.isNotEmpty(headerMap)) {
			headerMap.keySet().forEach(key -> this.headers.addAll(key, headerMap.get(key)));
		}
	}

	@Override
	public int getRawStatusCode() {
		return this.httpResponse.getStatusCode();
	}

	@Override
	public String getStatusText() throws IOException {
		return String.valueOf(this.getRawStatusCode());
	}

	@Override
	public void close() {
		Assert.notNull(httpResponse, "httpResponse cannot be null");
	}

	@Override
	public InputStream getBody() {
		return new ByteArrayInputStream(
				this.httpResponse.getContent().getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public HttpHeaders getHeaders() {
		return this.headers;
	}
}

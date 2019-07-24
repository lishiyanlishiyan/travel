package com.citsgbt.mobile.gateway.security.sso;

import com.citsamex.app.spi.data.caller.request.master.BaseCompanyParam;
import com.citsamex.app.spi.data.caller.response.company.CompanySsoConfigResponse;
import com.citsamex.app.spi.data.dto.company.SsoConfigDto;
import com.citsamex.app.spi.interfaces.targets.company.CompanyTargetService;
import com.citsgbt.mobile.core.spi.AxoLoginConsts;
import com.citsgbt.mobile.core.utils.lang.CoreUtils;
import com.citsgbt.mobile.core.web.vo.SsoParamsDto;
import com.citsgbt.mobile.gateway.config.oauth2.SimpleSsoConfigProperties;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static com.citsgbt.mobile.gateway.security.sso.utils.Keys.SSO_PARAMS_SAVE_KEY;

/**
 * @author gary.fu
 */
@Component
public class CompanySsoProviderImpl implements CompanySsoProvider {

	private static final Logger logger = LoggerFactory.getLogger(CompanySsoProviderImpl.class);

	@Autowired
	private CompanyTargetService companyTargetService;

	@Autowired
	private SimpleSsoConfigProperties simpleSsoConfigProperties;

	@Override
	public CompanySsoConfigResponse getSsoConfigResponse(String companyCode) {
		CompanySsoConfigResponse ssoConfigResponse = null;
		if (StringUtils.isNotBlank(companyCode)) {
			BaseCompanyParam param = new BaseCompanyParam();
			param.setCompanyCode(companyCode);
			ssoConfigResponse = companyTargetService.loadCompanySsoConfig(param);
			if (ssoConfigResponse != null && ssoConfigResponse.success() && CollectionUtils.isNotEmpty(ssoConfigResponse.getSsoConfigList())) {
				List<SsoConfigDto> ssoConfigList = ssoConfigResponse.getSsoConfigList().stream()
						.filter(ssoConfig -> StringUtils.isNotBlank(ssoConfig.getKey()) && ssoConfig.getKey().startsWith(AxoLoginConsts.SSO_KEY_PREFIX))
						.collect(Collectors.toList());
				ssoConfigResponse.setSsoConfigList(ssoConfigList);
			}
		}
		return ssoConfigResponse;
	}

	@Override
	public String buildRedirectUri(String clientId, String redirectUri) {
		try {
			URIBuilder uriBuilder = new URIBuilder("/oauth/authorize");
			uriBuilder.addParameter("response_type", "token");
			uriBuilder.addParameter("client_id", CoreUtils.nonBlank(clientId, simpleSsoConfigProperties.getDefaultClientId()));
			uriBuilder.addParameter("scope", "all");
			uriBuilder.addParameter("redirect_uri", CoreUtils.nonBlank(redirectUri, simpleSsoConfigProperties.getDefaultRedirectUri()));
			return uriBuilder.toString();
		} catch (URISyntaxException e) {
			logger.error("url解析错误", e);
		}
		return null;
	}

	@Override
	public void storeStatusAndRedirect(HttpServletRequest request, HttpServletResponse response, String clientId, String redirectUri) throws IOException {
		this.storeParams(request);
		String parsedRedirectUri = this.buildRedirectUri(clientId, redirectUri);
		response.sendRedirect(parsedRedirectUri);
	}

	protected void storeParams(HttpServletRequest request) {
		try {
			SsoParamsDto ssoParams = processParamDto(request);
			request.getSession().setAttribute(SSO_PARAMS_SAVE_KEY, ssoParams);
		} catch (Exception e) {
			logger.error("解析请求参数错误", e);
		}
	}

	protected SsoParamsDto processParamDto(HttpServletRequest request) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
		BeanInfo beanInfo = Introspector.getBeanInfo(SsoParamsDto.class);
		PropertyDescriptor[] beanProperties = beanInfo.getPropertyDescriptors();
		SsoParamsDto ssoParams = null;
		if (!ArrayUtils.isEmpty(beanProperties)) {
			for (int i = 0; i < beanProperties.length; i++) {
				PropertyDescriptor beanProperty = beanProperties[i];
				String key = beanProperty.getName();
				if (!"class".equals(key)) {
					String value = request.getParameter(key);
					if (StringUtils.isNotBlank(value)) {
						if (ssoParams == null) {
							ssoParams = new SsoParamsDto();
						}
						BeanUtils.setProperty(ssoParams, key, value);
					}
				}
			}
		}
		return ssoParams;
	}
}

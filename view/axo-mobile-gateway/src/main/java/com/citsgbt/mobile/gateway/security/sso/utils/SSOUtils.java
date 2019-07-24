package com.citsgbt.mobile.gateway.security.sso.utils;

import com.citsamex.app.spi.data.caller.response.company.CompanySsoConfigResponse;
import com.citsamex.app.spi.data.dto.company.SsoConfigDto;
import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.core.utils.lang.CoreUtils;
import com.citsgbt.mobile.gateway.security.sso.SsoType;
import com.citsgbt.mobile.gateway.security.sso.vo.*;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by david.jiang on 2015/6/12.
 */
public class SSOUtils {

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(SSOUtils.class);

    private static final Map<String, SsoConfigMapping> SSO_CONFIG_MAPPING;

    private static final Map<String, String> SAML_ENTITY_MAPPING = new ConcurrentHashMap<>();

    private static final Map<String, PropertiesConfig> PROPERTIES_CONFIG_MAP = new ConcurrentHashMap<>();

    private static final Pattern SSO_TYPE_PATTERN = Pattern.compile(".*/(\\w+)*$");

    /**
     * 单点登陆TokenKey
     */
    public static final String JWT_ACCESS_TOKEN_KEY = "access_token";

    @SuppressWarnings("squid:S1075")
    public static final String SSO_PATH_SSO = "/sso/{type}";
    @SuppressWarnings("squid:S1075")
    public static final String SSO_PATH_JWT = "/sso/jwt";

    static {
        SSO_CONFIG_MAPPING = Stream.of(SsoConfigMapping.of("userIDAttrName", "sso.userIDAttrName", "UserID"),
                SsoConfigMapping.of("companyIDAttrName", "sso.companyIDAttrName", "CompanyID"),
                SsoConfigMapping.of("verifySignatureNeeded", "sso.verifySignatureNeeded", "false"),
                SsoConfigMapping.of("relayState", "sso.relayState"),
                SsoConfigMapping.of("nameIDPolicyFormat", "sso.NameIDPolicy.Format", "")).collect(Collectors.toMap(SsoConfigMapping::getProperty, item -> item));
    }

    private SSOUtils() {

    }

    public static LoginConfig getCurrentLoginConfig() {
        HttpServletRequest request = HttpRequestUtils.getCurrentRequest();
        LoginConfig loginConfig = null;
        if (request != null) {
            loginConfig = (LoginConfig) request.getAttribute(Keys.SESSION_SSO_LOGIN_CONFIG);
        }
        return loginConfig;
    }

    public static void setCurrentLoginConfig(LoginConfig loginConfig) {
        HttpServletRequest request = HttpRequestUtils.getCurrentRequest();
        if (request != null) {
            request.setAttribute(Keys.SESSION_SSO_LOGIN_CONFIG, loginConfig);
        }
    }

    private static void setProperty(String companyCode, LoginConfig loginConfig, PropertiesConfig properties) {
        try {
            PropertyDescriptor[] propertyDescriptor = Introspector.getBeanInfo(loginConfig.getClass()).getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptor) {
                logger.debug("property: {}", property.getName());
                if (!"class".equals(property.getName())) {
                    String value = calcPropertyValue(companyCode, properties, property.getName());
                    BeanUtils.setProperty(loginConfig, property.getName(), value);
                }
            }
        } catch (Exception e) {
            logger.error("设置属性", e);
        }
    }

    private static String calcPropertyValue(String companyCode, PropertiesConfig properties, String propertyName) {
        SsoConfigMapping mapping = SSO_CONFIG_MAPPING.get(propertyName);
        String value;
        if (mapping != null) {
            value = properties.getPropertyValueIgnoreCase(mapping.getKey(), mapping.getDefaultValue());
        } else {
            value = getProperty(properties, companyCode, propertyName, "sso");
            if (StringUtils.isBlank(value)) {
                value = getProperty(properties, companyCode, propertyName, "WS");
            }
        }
        if (StringUtils.isBlank(value) && mapping != null) {
            value = mapping.getDefaultValue();
        }
        return value;
    }

    private static String getProperty(PropertiesConfig properties, String companyCode, String propertyName, String prefix) {
        String key = prefix + "." + propertyName;
        String value = properties.getPropertyValueIgnoreCase(key);
        if (StringUtils.isBlank(value)) {
            value = properties.getPropertyValueIgnoreCase(companyCode + "." + key);
        }
        return value;
    }

    public static LoginConfig getLoginConfig(CompanySsoConfigResponse response, String key) {
        LoginConfig loginConfig = null;
        if (response != null && CollectionUtils.isNotEmpty(response.getSsoConfigList())) {
            key = StringUtils.trimToEmpty(key);
            for (SsoConfigDto ssoConfigDto : response.getSsoConfigList()) {
                if (key.equalsIgnoreCase(StringUtils.trimToEmpty(ssoConfigDto.getKey()))) {
                    loginConfig = getLoginConfig(ssoConfigDto);
                    break;
                }
            }
        }
        return loginConfig;
    }

    public static LoginConfig getLoginConfig(CompanySsoConfigResponse response, SsoType ssoType) {
        if (ssoType != null && response != null) {
            for (SsoConfigDto ssoConfig : CoreUtils.wrapList(response.getSsoConfigList())) {
                LoginConfig loginConfig = getLoginConfig(ssoConfig);
                if (ssoConfig != null) {
                    SsoType type = SsoType.fromValue(ssoConfig.getType());
                    if (type == ssoType && isConfigSsoType(ssoType, loginConfig)) {
                        return loginConfig;
                    }
                }
            }
        }
        return null;
    }

    protected static boolean isConfigSsoType(SsoType ssoType, LoginConfig loginConfig) {
        return (ssoType == SsoType.SAML && loginConfig instanceof SAMLConfig)
                || (ssoType == SsoType.CAS && loginConfig instanceof CASConfig)
                || (ssoType == SsoType.OAUTH && loginConfig instanceof OauthConfig)
                || loginConfig instanceof CommonConfig;
    }

    public static LoginConfig getLoginConfig(CompanySsoConfigResponse response, String key, SsoType ssoType) {
        LoginConfig loginConfig;
        if (StringUtils.isNotBlank(key)) {
            loginConfig = getLoginConfig(response, key);
            if (loginConfig == null) {
                loginConfig = getLoginConfig(response, ssoType);
            }
        } else {
            loginConfig = getLoginConfig(response, ssoType);
            if (loginConfig == null) {
                loginConfig = getLoginConfig(response, key);
            }
        }
        return loginConfig;
    }

    /**
     * 处理properties文件
     *
     * @param fileName
     * @return
     */
    private static PropertiesConfig getPropertiesConfig(String fileName) {
        return PROPERTIES_CONFIG_MAP.computeIfAbsent(fileName, k -> new PropertiesConfig(fileName));
    }


    public static LoginConfig getLoginConfig(SsoConfigDto ssoConfig) {
        LoginConfig loginConfig = null;
        if (ssoConfig != null) {
            String companyCode = ssoConfig.getCompanyCode();
            loginConfig = new LoginConfig();
            String fileName = ssoConfig.getPropertyPath();
            SsoType ssoType = SsoType.fromValue(ssoConfig.getType());
            if (fileName.contains("/saml") || ssoType == SsoType.SAML) {
                loginConfig = new SAMLConfig();
            } else if (fileName.contains("/cas") || ssoType == SsoType.CAS) {
                loginConfig = new CASConfig();
            } else if (fileName.contains("/oath") || ssoType == SsoType.OAUTH) {
                loginConfig = new OauthConfig();
            } else if (ssoType == SsoType.COMMON || ssoType == SsoType.JWT || ssoType == SsoType.WS) {
                loginConfig = new CommonConfig();
            }
            PropertiesConfig propertiesConfig = getPropertiesConfig(fileName);
            setProperty(companyCode, loginConfig, propertiesConfig);
            loginConfig.setType(ssoType.getValue());
            loginConfig.setCompanyCode(companyCode);
            loginConfig.setKey(ssoConfig.getKey());
            loginConfig.setCompanyIp(ssoConfig.getIp());
            loginConfig.setOpenSsoRegister(ssoConfig.isOpenSsoRegister());
            if (loginConfig instanceof CommonConfig && ssoConfig.isNoValidateIp()) {
                loginConfig.setValidateIp(Boolean.FALSE.toString());
            }
            loginConfig.setAllowNormalLogin(Integer.valueOf(1).equals(ssoConfig.getNormalLoginFlag())
                    ? Boolean.toString(true) : Boolean.toString(false));
        }
        return loginConfig;
    }

    public static String populateCompanyId(ServletRequest servletRequest) {
        String companyId = null;
        Enumeration<?> requestParamNames = servletRequest.getParameterNames();
        while (requestParamNames.hasMoreElements()) {
            Object requestParamName = requestParamNames.nextElement();
            if ("companyid".equalsIgnoreCase(requestParamName.toString())) {
                companyId = servletRequest.getParameter(requestParamName.toString());
                break;
            }
        }
        return companyId;
    }

    public static String getPeerEntityId(LoginConfig loginConfig, String defaultEntityId) {
        if (loginConfig instanceof SAMLConfig) {
            if (StringUtils.isNotBlank(((SAMLConfig) loginConfig).getEntityId())) {
                return ((SAMLConfig) loginConfig).getEntityId();
            }
            return getSamlEntityId(StringUtils.join("idp-", loginConfig.getCompanyCode(),
                    "-", StringUtils.isBlank(loginConfig.getKey()) ? "default" : StringUtils.trimToEmpty(loginConfig.getKey())));
        }
        return defaultEntityId;
    }


    public static String getEntityId(LoginConfig loginConfig, String defaultEntityId) {
        if (loginConfig instanceof SAMLConfig) {
            return StringUtils.join(defaultEntityId, "/", loginConfig.getCompanyCode());
        }
        return defaultEntityId;
    }

    /**
     * saml自动生成的id和实际id映射
     *
     * @param entityId
     * @param newEntityId
     */
    public static void putSamlEntityId(String entityId, String newEntityId) {
        SAML_ENTITY_MAPPING.put(entityId, newEntityId);
    }

    /**
     * @param entityId
     * @return
     */
    private static String getSamlEntityId(String entityId) {
        String newEntityId = SAML_ENTITY_MAPPING.get(entityId);
        return StringUtils.isNotBlank(newEntityId) ? newEntityId : entityId;
    }

    /**
     * 根据请求来获取
     *
     * @param requestPath
     * @return
     */
    public static SsoType getSsoTypeByPath(String requestPath) {
        SsoType ssoType = null;
        Matcher matcher = SSO_TYPE_PATTERN.matcher(requestPath);
        if (matcher.find()) {
            String type = matcher.group(1);
            ssoType = SsoType.fromValue(type);
        }
        return ssoType;
    }

    /**
     * ip地址验证
     *
     * @param ipPattern
     * @param ipAddress
     * @return
     */
    public static boolean validateIpAddress(String ipPattern, String ipAddress) {
        if ("127.0.0.1".equals(ipAddress)) {
            return true;
        }
        if (StringUtils.isNotBlank(ipPattern)) {
            if (StringUtils.equals("*", ipPattern)) {
                return true;
            } else {
				String ipSeg = ipAddress;
				if (ipAddress.indexOf('.') > -1) {
					ipSeg = ipAddress.substring(0, ipAddress.lastIndexOf('.')) + ".*";
				}
                String[] ips = ipPattern.split("\\s*;\\s*");
                return ArrayUtils.contains(ips, ipAddress) || ArrayUtils.contains(ips, ipSeg);
            }
        }
        return false;
    }

    public static URI buildOauthUri(OAuth2AuthorizationRequest authorizationRequest) {
        Assert.notNull(authorizationRequest, "authorizationRequest cannot be null");
        Set<String> scopes = authorizationRequest.getScopes();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(authorizationRequest.getAuthorizationUri())
                .queryParam(OAuth2ParameterNames.RESPONSE_TYPE, authorizationRequest.getResponseType().getValue())
                .queryParam(OAuth2ParameterNames.CLIENT_ID, authorizationRequest.getClientId())
                .queryParam(OAuth2ParameterNames.SCOPE, org.springframework.util.StringUtils.collectionToDelimitedString(scopes, " "))
                .queryParam(OAuth2ParameterNames.STATE, authorizationRequest.getState());
        if (authorizationRequest.getRedirectUri() != null) {
            uriBuilder.queryParam(OAuth2ParameterNames.REDIRECT_URI, authorizationRequest.getRedirectUri());
        }

        return uriBuilder.build().encode().toUri();
    }

    public static boolean isOauthResponse(HttpServletRequest request) {
        return (StringUtils.isNotBlank(request.getParameter(OAuth2ParameterNames.CODE)) &&
                StringUtils.isNotBlank(request.getParameter(OAuth2ParameterNames.STATE)))
                || (StringUtils.isNotBlank(request.getParameter(OAuth2ParameterNames.ERROR)) &&
                StringUtils.isNotBlank(request.getParameter(OAuth2ParameterNames.STATE)));
    }

    public static void processOauthAdditionalHeaders(HTTPRequest request) {
        LoginConfig loginConfig = getCurrentLoginConfig();
        if (loginConfig instanceof OauthConfig) {
            String additionHeaders = ((OauthConfig) loginConfig).getAdditionHeaders();
            if (StringUtils.isNotBlank(additionHeaders)) {
                List<NameValuePair> items = URLEncodedUtils.parse(additionHeaders, StandardCharsets.UTF_8);
                for (NameValuePair item : items) {
                    request.setHeader(item.getName(), item.getValue());
                }
            }
        }
    }
}


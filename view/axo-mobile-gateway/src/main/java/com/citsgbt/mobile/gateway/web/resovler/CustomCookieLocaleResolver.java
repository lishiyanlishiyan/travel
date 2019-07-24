package com.citsgbt.mobile.gateway.web.resovler;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by Gary Fu on 16/10/12.
 */
public class CustomCookieLocaleResolver extends CookieLocaleResolver {

    /**
     * 支持的Locale
     */
    private Set<Locale> supportLocales = new HashSet<>(Arrays.asList(Locale.SIMPLIFIED_CHINESE, Locale.US));

    private static final String DEFAULT_PARAM_NAME = "axo_locale";

    private String paramName = DEFAULT_PARAM_NAME;

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        parseLocaleParamIfNecessary(request);
        return super.resolveLocale(request);
    }

    @Override
    public LocaleContext resolveLocaleContext(HttpServletRequest request) {
        parseLocaleParamIfNecessary(request);
        return super.resolveLocaleContext(request);
    }

    private void parseLocaleParamIfNecessary(HttpServletRequest request) {
        String localeStr = getRequestLanguage(request);
        if (request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME) == null && StringUtils.isNotBlank(localeStr)) {
            Locale locale = null;
            try {
                locale = LocaleUtils.toLocale(localeStr);
            } catch (Exception e) {
                logger.error("从参数获取Locale失败", e);
            }
            TimeZone timeZone = TimeZone.getDefault();
            if (isSupport(locale)) {
                request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, locale);
                request.setAttribute(TIME_ZONE_REQUEST_ATTRIBUTE_NAME, (timeZone != null ? timeZone : determineDefaultTimeZone(request)));
            }
        }
    }

    /**
     * 获取语言
     *
     * @param request
     * @return
     */
    private String getRequestLanguage(HttpServletRequest request) {
        String language = request.getParameter(getParamName());
        if (StringUtils.isBlank(language)) {
            language = request.getParameter("ssoLanguage");
            if (StringUtils.isNotBlank(language)) {
                if (language.startsWith("zh_") || language.equals("chs") || language.equalsIgnoreCase("CN")) {
                    language = "zh_CN";
                } else {
                    language = "en_US";
                }
            }
        }
        return language;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        if (!isSupport(locale)) {
            locale = null;
        }
        super.setLocale(request, response, locale);
    }

    private boolean isSupport(Locale locale) {
        return locale != null && supportLocales.contains(locale);
    }

    public Set<Locale> getSupportLocales() {
        return supportLocales;
    }

    public void setSupportLocales(Set<Locale> supportLocales) {
        this.supportLocales = supportLocales;
    }

    private String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
}

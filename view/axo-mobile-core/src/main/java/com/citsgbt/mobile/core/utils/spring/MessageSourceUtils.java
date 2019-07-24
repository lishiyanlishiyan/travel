/**
 *
 */
package com.citsgbt.mobile.core.utils.spring;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * @author gary.fu
 *
 */
@Component
public class MessageSourceUtils {

    private static final Logger logger = LoggerFactory.getLogger(MessageSourceUtils.class);

    private static MessageSource messageSource;

    @Autowired
    public void injectMessageSource(MessageSource ms){
        setMessageSource(ms);
    }

    public static void setMessageSource(MessageSource ms) {
        messageSource = ms;
    }

    /**
     * 是否已经初始化
     *
     * @return
     */
    public static boolean isInited() {
        return getMessageSource() != null;
    }

    /**
     * 获取MessageSource bean
     *
     * @return
     */
    private static MessageSource getMessageSource() {
        if (messageSource == null && ApplicationContextUtils.isInited()) {
            try {
                messageSource = ApplicationContextUtils.getBean(MessageSource.class);
            } catch (BeansException e) {
                logger.error("Bean Error", e);
            }
        }
        return messageSource;
    }

    /**
     * 获取消息工具
     *
     * @see org.springframework.context.MessageSource#getMessage(String, Object[], String, Locale)
     * @param code
     *            代码
     * @param defaultMessage
     *            默认消息
     * @param locale
     *            语言
     * @param args
     *            参数
     * @return
     */
    public static String getMessage(String code, String defaultMessage, Locale locale, Object... args) {
        if (isInited()) {
            return messageSource.getMessage(code, args, defaultMessage, locale);
        } else {
            return "";
        }
    }

    /**
     * 获取消息
     *
     * @param code
     *            代码
     * @param locale
     *            语言
     * @param args
     *            参数
     * @return
     */
    private static String getMessage(String code, Locale locale, Object... args) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        if (isInited()) {
            return messageSource.getMessage(code, args, locale);
        } else {
            return "";
        }
    }

    /**
     * 获取国际化消息
     *
     * @param code
     * @param locale
     * @param args
     * @return
     */
    public static String getMessage(String code, String locale, Object... args) {
        Locale loc = null;
        try {
            if (StringUtils.isNotBlank(locale)) {
                loc = LocaleUtils.toLocale(locale);
            }
        } catch (Exception e) {
            logger.error("Message Error", e);
        }
        return getMessage(code, loc, args);
    }

    /**
     * 获取国际化消息
     *
     * @param code
     * @param args
     * @return
     */
    public static String getMessage(String code, Object... args) {
        return getMessage(code, LocaleContextHolder.getLocale(), args);
    }
}

/**
 *
 */
package com.citsgbt.mobile.core.utils.spring;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Gary.Fu
 *
 */
@Component
public class ApplicationContextUtils implements ApplicationContextAware,
        DisposableBean {

    /**
     * 容器启动的时候注入Spring ApplicationContext
     */
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        setContext(applicationContext);
    }

    /**
     * 判断是否初始化完成
     */
    public static boolean isInited() {
        return applicationContext != null;
    }

    /**
     * 获取Context
     *
     * @return
     */
    private static ApplicationContext getContext() {
        Assert.notNull(applicationContext,
                "ApplicationContext isn't inited, ApplicationContextUtils is not available");
        return applicationContext;
    }

    /**
     * 设置ApplicationContext
     *
     * @param context
     */
    private static void setContext(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * 按照名称获取Bean
     *
     * @param name
     * @return
     * @see org.springframework.context.ApplicationContext#getBean(String)
     */
    public static Object getBean(String name) {
        return getContext().getBean(name);
    }

    /**
     * 按照类型获取Bean
     *
     * @param requiredType
     * @return
     * @throws org.springframework.beans.BeansException
     * @see {@link org.springframework.context.ApplicationContext#getBean(Class)}
     */
    public static <T> T getBean(Class<T> requiredType) {
        return getContext().getBean(requiredType);
    }

    /**
     * @param name
     * @param requiredType
     * @return
     * @throws org.springframework.beans.BeansException
     * @see org.springframework.context.ApplicationContext#getBean(String, Class)
     */
    public static <T> T getBean(String name, Class<T> requiredType) {
        return getContext().getBean(name, requiredType);
    }

    /**
     * 发布Spring Event事件
     *
     * @param event
     * @see org.springframework.context.ApplicationContext#publishEvent(org.springframework.context.ApplicationEvent)
     */
    public static void publishEvent(ApplicationEvent event) {
        getContext().publishEvent(event);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    @Override
    public void destroy() {
        setContext(null);
    }
}

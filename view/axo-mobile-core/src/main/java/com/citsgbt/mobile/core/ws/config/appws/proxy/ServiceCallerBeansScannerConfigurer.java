package com.citsgbt.mobile.core.ws.config.appws.proxy;

import com.citsamex.app.spi.annotation.CallerServicePath;
import com.citsamex.app.spi.interfaces.TargetService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;


/**
 * 自动扫描TargetService子类,并且代理配置成Bean
 * Created by Gary Fu
 */
public class ServiceCallerBeansScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private String basePackages;

    private Class<? extends Annotation> includeAnnotationClass = CallerServicePath.class;

    private Class<?> markerInterface = TargetService.class;

    private BeanNameGenerator nameGenerator;

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {

    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        ServiceCallerBeansScanner scanner = new ServiceCallerBeansScanner(registry);
        scanner.setIncludeAnnotationClass(this.includeAnnotationClass);
        scanner.setResourceLoader(this.applicationContext);
        scanner.setBeanNameGenerator(this.nameGenerator);
        scanner.setMarkerInterface(markerInterface);
        scanner.registerFilters();
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackages, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(basePackages, "需要扫描的包路径不能为空");
    }

    public String getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(String basePackages) {
        this.basePackages = basePackages;
    }

    public Class<? extends Annotation> getIncludeAnnotationClass() {
        return includeAnnotationClass;
    }

    public void setIncludeAnnotationClass(Class<? extends Annotation> includeAnnotationClass) {
        this.includeAnnotationClass = includeAnnotationClass;
    }

    public BeanNameGenerator getNameGenerator() {
        return nameGenerator;
    }

    public void setNameGenerator(BeanNameGenerator nameGenerator) {
        this.nameGenerator = nameGenerator;
    }

    public Class<?> getMarkerInterface() {
        return markerInterface;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }
}

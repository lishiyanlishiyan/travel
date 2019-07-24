package com.citsgbt.mobile.core.ws.config.appws.proxy;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 配置自动扫描基于TargetService的client服务
 * 使用ServiceCallerProxyFactoryBean自动配置成spring bean方便使用
 * <p>
 * Created by Gary Fu
 */
class ServiceCallerBeansScanner extends ClassPathBeanDefinitionScanner {

    private static final Logger loggerLocal = getLogger(ServiceCallerBeansScanner.class);

    private Class<? extends Annotation> includeAnnotationClass;

    private Class<?> markerInterface;

    public ServiceCallerBeansScanner(BeanDefinitionRegistry registry) {
        super(registry, false); // 自定义扫描规则
    }

    public void registerFilters() {
        // 默认只扫描 CallerServicePath注解的接口
        addIncludeFilter(new AnnotationTypeFilter(getIncludeAnnotationClass()));
        // override AssignableTypeFilter to ignore matches on the actual marker interface
        if (this.markerInterface != null) {
            addIncludeFilter(new AssignableTypeFilter(this.markerInterface));
        }
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        if (!beanDefinitionHolders.isEmpty()) {
            for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitionHolders) {
                GenericBeanDefinition definition = (GenericBeanDefinition) beanDefinitionHolder.getBeanDefinition();
                loggerLocal.info("Registering AppWs Bean [{}]", definition.getBeanClassName());
                definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName()); // 构造函数传入class名称
                definition.setBeanClass(ServiceCallerProxyFactoryBean.class);
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            }
        }
        return beanDefinitionHolders;
    }

    /**
     * 复写Spring的默认扫描模式,忽略接口方式
     *
     * @param beanDefinition
     * @return
     * @throws IOException
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    private Class<? extends Annotation> getIncludeAnnotationClass() {
        return includeAnnotationClass;
    }

    public void setIncludeAnnotationClass(Class<? extends Annotation> includeAnnotationClass) {
        this.includeAnnotationClass = includeAnnotationClass;
    }

    public Class<?> getMarkerInterface() {
        return markerInterface;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }
}

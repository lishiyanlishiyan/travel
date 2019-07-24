/**
 *
 */
package com.citsgbt.mobile.core.utils.lang;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * DozerMapper构建
 *
 * @author back_cloud_003
 */
@Component
public class DozerUtils implements DisposableBean {

    /**
     * DozerMapper
     */
    private static Mapper mapper;

    @Autowired(required = false)
    public void injectMapper(Mapper mapper) {
        setMapper(mapper);
    }

    /**
     * 获取Mapper
     *
     * @return the mapper
     */
    private static Mapper getMapper() {
        if (mapper == null) {
            mapper = new DozerBeanMapper();
        }
        return mapper;
    }

    /**
     * 使用source映射给target，复制属性
     *
     * @param source
     * @param target
     * @return
     */
    public static void map(Object source, Object target) {
        getMapper().map(source, target);
    }

    /**
     * 使用source映射给target，复制属性
     *
     * @param source
     * @param target
     * @return
     */
    public static void map(Object source, Object target, String mapId) {
        getMapper().map(source, target, mapId);
    }

    /**
     * 使用source映射给targetClass，自动生成对象并复制属性
     *
     * @param source
     * @param targetClass
     * @return
     */
    public static <T> T map(Object source, Class<T> targetClass) {
        if (source == null)
            return null;
        return getMapper().map(source, targetClass);
    }

    /**
     * 把sourceLst映射成targetClass类型的ArrayList返回
     *
     * @param sourceLst
     * @param targetClass
     * @return
     */
    public static <T> List<T> mapList(List sourceLst, Class<T> targetClass) {
        if (sourceLst == null)
            return new ArrayList<>();
        List<T> returnLst = new ArrayList<>();
        for (Object source : sourceLst) {
            returnLst.add(getMapper().map(source, targetClass));
        }
        return returnLst;
    }

    /**
     * 使用source映射给targetClass，自动生成对象并复制属性
     *
     * @param source
     * @param targetClass
     * @param mapId
     * @return
     */
    public static <T> T map(Object source, Class<T> targetClass, String mapId) {
        return getMapper().map(source, targetClass, mapId);
    }

    /**
     * @param mpr
     */
    private static void setMapper(Mapper mpr) {
        mapper = mpr;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    @Override
    public void destroy() {
        setMapper(null);
    }

}

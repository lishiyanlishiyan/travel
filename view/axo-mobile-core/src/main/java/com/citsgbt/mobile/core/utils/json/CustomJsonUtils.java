package com.citsgbt.mobile.core.utils.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by gary.fu on 2016/10/25.
 */
public class CustomJsonUtils {

	private CustomJsonUtils() {

	}

	private static final Logger logger = LoggerFactory.getLogger(CustomJsonUtils.class);
	/**
	 * 默认Mapper
	 */
	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		// 注册jaxb annotation解析模块
		JaxbAnnotationModule module = new JaxbAnnotationModule();
		// configure as necessary
		mapper.registerModule(module);
		// 配置日期格式
		// mapper.setDateFormat(new SimpleDateFormat(
		mapper.disable(SerializationFeature.INDENT_OUTPUT); // 去掉缩进
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 不解析null的值
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.disable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME);
		// jaxb annotation 解析
		AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
		// Jackson annotation 解析
		AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
		// XXX 注册jaxb之后默认就是jaxb优先，顺序可以调换
		AnnotationIntrospector introspector = AnnotationIntrospector.pair(jaxbIntrospector, jacksonIntrospector);
		// make deserializer use JAXB jackson annotations
		mapper.getDeserializationConfig().with(introspector);
		// make serializer use JAXB jackson annotations
		mapper.getSerializationConfig().with(introspector);
	}

	/**
	 * 输出到Json字符串,
	 *
	 * @param input
	 * @return
	 */
	public static String toJsonUTF8(Object input) {

		String result = StringUtils.EMPTY;
		try (ByteArrayOutputStream bio = new ByteArrayOutputStream(); JsonGenerator jsonGenerator = mapper.getFactory().createGenerator(bio)) {
			jsonGenerator.writeObject(input);
			result = bio.toString("UTF-8");
		} catch (Exception e) {
			logger.error("输出Json错误", e);
		}
		return result;
	}

	/**
	 * 输出到Json字符串
	 *
	 * @param input
	 * @return
	 */
	public static String toJson(Object input) {
		String result = StringUtils.EMPTY;
		try {
			result = mapper.writeValueAsString(input);
		} catch (Exception e) {
			logger.error("输出Json错误", e);
		}
		return result;
	}

	/**
	 * json字符串转对象
	 *
	 * @param json
	 * @param clazz
	 * @return
	 */
	public static <T> T fromJson(String json, Class<T> clazz) {
		T result = null;
		try {
			result = mapper.readValue(json, clazz);
		} catch (Exception e) {
			logger.error("解析Json错误", e);
		}
		return result;
	}

	/**
	 * 列表json读取
	 *
	 * @param json
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	public static <T> T fromJsonToList(String json, Class<T> clazz) {
		T result = null;
		try {
			JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, clazz);
			result = mapper.readValue(json, javaType);
		} catch (Exception e) {
			logger.error("解析Json错误", e);
		}
		return result;
	}

	/**
	 * 获取mapper对象
	 *
	 * @return
	 */
	public static ObjectMapper getMapper() {
		return mapper;
	}
}

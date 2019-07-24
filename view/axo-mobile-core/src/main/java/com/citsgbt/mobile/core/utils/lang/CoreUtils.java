/**
 *
 */
package com.citsgbt.mobile.core.utils.lang;

import com.citsgbt.mobile.core.spi.CoreConsts;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 常用工具类
 *
 * @author back_cloud_003
 */
public class CoreUtils {

	private CoreUtils() {
	}

	private static final Logger logger = LoggerFactory.getLogger(CoreUtils.class);
	private static final String REG_SPACE = "\\s*{0}\\s*";
	private static final String REG_TRANS = "\\{0}";
	private static final String NEED_TRANS = "[|\\\\?]";
	private static final String REG_CHINESE = "[\u4e00-\u9fa5（）《》——；，。“”]+";
	private static final String MATCH_SPLITTER = "[.:/\\s]+";
	private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

	/**
	 * 把特殊字符转成正则表达式形式，常用在split
	 *
	 * @param s
	 * @return
	 */
	public static String s2reg(String s) {
		s = StringUtils.trimToEmpty(s);
		if (s.matches(NEED_TRANS)) {
			s = MessageFormat.format(REG_TRANS, s);
		}
		s = MessageFormat.format(REG_SPACE, s);
		return s;
	}

	/**
	 * 添加item，不重复
	 *
	 * @param items
	 * @param item
	 */
	public static <T> void add(Collection<T> items, T item) {
		if (!items.contains(item)) {
			items.add(item);
		}
	}

	/**
	 * 如果item不为空则添加
	 *
	 * @param items
	 * @param item
	 */
	public static <T> void addIfNotNull(Collection<T> items, T item) {
		if (item != null) {
			items.add(item);
		}
	}

	/**
	 * 包装List
	 *
	 * @param items
	 * @return
	 */
	public static <T> List<T> wrapList(List<T> items) {
		if (items == null) {
			return newList();
		}
		return items;
	}

	/**
	 * 新<code>new ArrayList();</code>
	 *
	 * @return
	 */
	private static <T> List<T> newList() {
		return new ArrayList<>();
	}

	/**
	 * 新<code>new ArrayList(items);</code>
	 *
	 * @return
	 */
	public static <T> List<T> newList(Collection<T> items) {
		return new ArrayList<>(items);
	}

	/**
	 * 新<code>new HashMap();</code>
	 *
	 * @return
	 */
	public static <K, V> Map<K, V> newMap() {
		return new HashMap<>();
	}

	/**
	 * @param s
	 * @return
	 */
	private static String trim(String s) {
		return StringUtils.trimToEmpty(s);
	}

	/**
	 * 组装URI和参数
	 *
	 * @param uri
	 * @param params
	 * @return
	 */
	public static String uriParam(String uri, String... params) {
		uri = trim(uri);
		if (ArrayUtils.isNotEmpty(params)) {
			StringBuilder uriBuilder = new StringBuilder(uri);
			for (String param : params) {
				if (StringUtils.isBlank(param)) {
					continue;
				}
				String sep = CoreConsts.AND;
				if (!uriBuilder.toString().contains(CoreConsts.WEN)) {
					sep = CoreConsts.WEN;
				}
				uriBuilder.append(sep).append(param);
			}
			uri = uriBuilder.toString();
		}
		return uri;
	}

	/**
	 * 解析2013-02-27T14:35:06.171875+08:00长日期，缩短成java能解析的格式
	 *
	 * @param date
	 * @return
	 */
	public static String parseCSDate(String date) {
		if (date != null && date.length() > 26) {
			Pattern pattern = Pattern.compile("(.*\\.)(\\d+)(\\+.*)");
			Matcher matcher = pattern.matcher(date);
			if (matcher.find() && matcher.groupCount() == 3) {
				String prefix = matcher.group(1);
				String ms = matcher.group(2);
				String subfix = matcher.group(3);
				if (ms.length() > 3) {
					ms = ms.substring(0, 3);
				}
				date = prefix + ms + subfix;
			}
		}
		return date;
	}

	/**
	 * 获取futures对象
	 *
	 * @param futures
	 * @return
	 */
	private static <T> List<T> transferFutures(List<Future<T>> futures) {
		List<T> result = newList();
		for (Future<T> future : futures) {
			try {
				result.add(future.get());
			} catch (InterruptedException | ExecutionException e) {
				logger.error("等待Future错误", e);
				Thread.currentThread().interrupt();
			}
		}
		return result;
	}

	/**
	 * 获取futures对象
	 *
	 * @param futures
	 * @return
	 */
	@SafeVarargs
	public static <T> List<T> transferFutures(Future<T>... futures) {
		return transferFutures(Arrays.asList(futures));
	}

	/**
	 * 获取一个uuid字符串
	 *
	 * @return
	 */
	public static String uuid() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * 判断是否是json格式
	 *
	 * @param str
	 * @return
	 */
	public static boolean isJson(String str) {
		return StringUtils.trimToEmpty(str).startsWith("{");
	}

	/**
	 * 判断是否是xml格式
	 *
	 * @param str
	 * @return
	 */
	public static boolean isXml(String str) {
		return StringUtils.trimToEmpty(str).startsWith("<");
	}

	public static void main(String[] args) {
		throw new UnsupportedOperationException();
	}

	public static boolean replaceAllChineseCharacter(String source) {
		if (StringUtils.isNotEmpty(source)) {
			Pattern p = Pattern.compile(REG_CHINESE);
			Matcher m = p.matcher(source);
			return m.find();
		} else {
			return false;
		}
	}

	public static String str2Date2Str(String datestr, String fmtstr) {
		if (datestr != null && !"".equals(datestr)) {
			DateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			DateFormat format1 = new java.text.SimpleDateFormat(fmtstr);
			try {
				return format1.format(format.parse(datestr));
			} catch (ParseException e) {
				logger.debug("日期转换出错", e);
			}
		}
		return datestr;

	}

	/**
	 * @param source
	 * @param len
	 * @param padStr
	 * @param left
	 * @return
	 */
	public static String fillOrRemove(String source, int len, String padStr,
									  boolean left) {
		source = source == null ? StringUtils.EMPTY : source;
		if (left) {
			return StringUtils.leftPad(StringUtils.left(source, len), len,
					padStr);
		} else {
			return StringUtils.rightPad(StringUtils.right(source, len), len,
					padStr);
		}
	}

	public static String getNonOrderByPart(String sql) {
		String loweredString = sql.toLowerCase();
		int orderByIndex = loweredString.lastIndexOf("order by");
		if (orderByIndex != -1) {
			return sql.substring(0, orderByIndex - 1);
		} else {
			return sql;
		}
	}

	private static String getListText(Collection<String> items, String separator) {
		if (items != null && !items.isEmpty()) {
			List<String> collect = items.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
			return StringUtils.join(collect, separator);
		}
		return StringUtils.EMPTY;
	}

	public static String getListText(Collection<String> items) {
		return getListText(items, CoreConsts.SPACE);
	}

	public static List<String> splitByWord(String input, int len) {
		List<String> result = new ArrayList<>();
		if (StringUtils.isNotBlank(input)) {
			String[] items = input.split(MATCH_SPLITTER);
			for (int i = 0; i < items.length; i++) {
				if (i + len <= items.length) {
					List<String> subItems = new ArrayList<>(Arrays.asList(items).subList(i, len + i));
					result.add(StringUtils.join(subItems, CoreConsts.SPACE));
				}
			}
		}
		return result;
	}

	public static boolean stringMatch(String input, String keyword) {
		boolean result = StringUtils.containsIgnoreCase(input, keyword);
		if (!result) {
			input = input.replaceAll(MATCH_SPLITTER, CoreConsts.SPACE); // 处理一些特殊分割字符
			result = StringUtils.containsIgnoreCase(input, keyword);
		}
		return result;
	}

	/**
	 * Steam 去重复处理
	 *
	 * @param keyExtractor
	 * @param <T>
	 * @return
	 */
	public static <T> java.util.function.Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return object -> seen.putIfAbsent(keyExtractor.apply(object), Boolean.TRUE) == null;
	}

	/**
	 * 转化成 '1','2'格式，用于sql的in条件
	 *
	 * @param items
	 * @return
	 */
	public static String inCondition(Collection<?> items) {
		return items.stream().map(String::valueOf).distinct().collect(Collectors.joining("','", "'", "'"));
	}

	/**
	 *
	 * @param textList
	 * @param separator
	 * @return
	 */
	private static String getListText(List<String> textList, String separator) {
		if (CollectionUtils.isNotEmpty(textList)) {
			return StringUtils.join(textList.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()), separator);
		}
		return StringUtils.EMPTY;
	}

	public static String getListText(List<String> textList) {
		return getListText(textList, CoreConsts.SPACE);
	}

	/**
	 * 模式匹配
	 *
	 * @param pattern
	 * @param value
	 * @return
	 */
	public static boolean match(String pattern, String value) {
		logger.info("处理做账{}匹配{}", pattern, value);
		return PATH_MATCHER.match(pattern, value);
	}

	/**
	 * 按照属性规则解析值
	 *
	 * @param bean
	 * @param propertyNamePattern
	 * @return
	 */
	public static String parsePropertyPattern(Object bean, String propertyNamePattern) {
		if (StringUtils.isNotBlank(propertyNamePattern)) {
			StringBuilder sb = new StringBuilder();
			String[] properties = propertyNamePattern.split("\\s*/\\s*");
			Arrays.stream(properties).forEach(property -> {
				String value = null;
				try {
					value = BeanUtils.getProperty(bean, property);
				} catch (Exception e) {
					logger.error("属性获取错误", e);
				} finally {
					appendValue(sb, value);
				}
			});
			return sb.toString();
		}
		return CoreConsts.VALUE_UNKNOWN;
	}

	/**
	 * 构建值
	 *
	 * @param sb
	 * @param value
	 * @return
	 */
	private static void appendValue(StringBuilder sb, String value) {
		if (sb.length() > 0) {
			sb.append(CoreConsts.XIE);
		}
		sb.append(StringUtils.isBlank(value) ? CoreConsts.VALUE_UNKNOWN : StringUtils.trim(value));
	}

	/**
	 * 返回不为空数据
	 * @param property
	 * @param values
	 * @return
	 */
	public static String nonBlank(String property, String... values) {
		if (StringUtils.isBlank(property)) {
			for (String value : values) {
				if (StringUtils.isNotBlank(value)) {
					return value;
				}
			}
		}
		return property;
	}

	public static String[] parseConfig(String config, String defaultConfig) {
		config = StringUtils.isNotBlank(config) ? config : defaultConfig;
		String[] result = new String[0];
		if (StringUtils.isNotBlank(config)) {
			result = config.split("\\s*,\\s*");
		}
		return result;
	}

	public static String[] parseConfig(String config) {
		return parseConfig(config, null);
	}

}

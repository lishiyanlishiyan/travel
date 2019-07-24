package com.citsgbt.mobile.open.test.logger;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerTest {

	private static final Logger logger = LoggerFactory.getLogger(LoggerTest.class);

	@Test
	public void test() {
		String name = "Test";
		Assert.assertEquals("Test", name);
		logger.info("测试日志输出{}", name);
	}
}

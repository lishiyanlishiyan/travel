package com.citsgbt.mobile.core.spi;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Created by gary.fu on 2014/6/3.
 */
public enum ProductType {

	ORDER("order"),// 大订单
	HOTEL("hotel"),
	DOM_AIR("domair"),
	INTL_AIR("intlair"),
	TRAIN("train"),
	OTHERS("others"),
	VISA("visa"),
	DOM_AIR_REFUND("domair-refund"),
	DOM_AIR_CANCEL("domair-cancel"),
	DOM_AIR_REBOOK("domair-rebook"),
	CAR_RENTAL("car-rental"),
	INTL_AIR_REBOOK("intlAir-rebook"),
	INTL_AIR_REFUND("intlAir-refund"),
	ALL("all");

	private final String value;

	ProductType(final String prodType) {
		this.value = prodType;
	}

	public static ProductType fromValue(String prodType) {
		Validate.notBlank(prodType, "ProductType不能为空");
		for (ProductType productType : ProductType.values()) {
			if (StringUtils.equalsIgnoreCase(prodType, productType.value)) {
				return productType;
			}
		}
		return null;
	}

	/**
	 * 获取String值
	 *
	 * @return
	 */
	public String getValue() {
		return value;
	}
}

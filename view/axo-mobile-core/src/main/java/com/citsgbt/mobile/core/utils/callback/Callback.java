package com.citsgbt.mobile.core.utils.callback;

/**
 * Created on 2015/8/31 9:51.<br>
 *
 * @author gary.fu
 */
@FunctionalInterface
public interface Callback<T> {

	/**
	 * 回调数据
	 *
	 * @param data
	 */
	void doCallback(T data);
}

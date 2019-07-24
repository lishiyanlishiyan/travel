package com.citsgbt.mobile.open.service.caller;

import com.citsgbt.mobile.core.ws.config.appws.ServiceCallerInfo;

/**
 * @author gary.fu
 */
public interface ServiceCallerConfigService {

	/**
	 * 获取服务信息
	 *
	 * @param servicePath
	 * @param serviceName
	 * @return
	 */
	ServiceCallerInfo locateTargetParam(String servicePath, String serviceName);

}

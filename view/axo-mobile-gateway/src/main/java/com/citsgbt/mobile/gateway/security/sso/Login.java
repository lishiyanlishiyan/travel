package com.citsgbt.mobile.gateway.security.sso;

import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登陆接口
 */
public interface Login {

	/**
	 * 登陆
	 *
	 * @param request
	 * @param response
	 * @param loginConfig
	 * @return
	 */
	LoginVo login(HttpServletRequest request, HttpServletResponse response, LoginConfig loginConfig);
}

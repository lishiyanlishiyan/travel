package com.citsgbt.mobile.open.service.login;

import com.citsgbt.mobile.open.web.vo.LoginResultVo;

/**
 * @author gary
 */
public interface InitLoginService {

    void initLogin(LoginResultVo loginResult);

    void reCalcLoginResult(LoginResultVo loginResult, String companyCode, String userId);
}

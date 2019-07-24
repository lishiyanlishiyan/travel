package com.citsgbt.mobile.gateway.security.sso.common;

import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.core.utils.spring.MessageSourceUtils;
import com.citsgbt.mobile.gateway.security.sso.Login;
import com.citsgbt.mobile.gateway.security.sso.SsoLoginResultVo;
import com.citsgbt.mobile.gateway.security.sso.exception.SsoException;
import com.citsgbt.mobile.gateway.security.sso.utils.Keys;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.CommonConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 基础sso登陆
 *
 * @author gary.fu
 */
public abstract class AbstractLogin implements Login {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LocaleResolver localeResolver;

    private String getIpAddress(HttpServletRequest request) {
        return HttpRequestUtils.getIp(request);
    }

    @Override
    public LoginVo login(HttpServletRequest request, HttpServletResponse response, LoginConfig loginConfig) {
        LoginVo loginVo = new LoginVo(loginConfig.getCompanyCode());
        Map<String, Object> inputMap = processInputMap(request);
        SsoLoginResultVo resultVo = new SsoLoginResultVo(loginConfig, inputMap, loginVo);
        resultVo.setIpAddress(getIpAddress(request));
        request.setAttribute(Keys.SSO_LOGIN_RESULT_KEY, resultVo);
        if (!isNeedValidateIp(loginConfig) || validateIpAddress(request, loginConfig)) {
            try {
                loginVo = doLogin(request, response, loginConfig);
            } catch (SsoException e) {
                throw e;
            } catch (Exception e) {
                throw new SsoException(MessageSourceUtils.getMessage("sso.invalid.no.principal"), e, loginConfig);
            }
        }
        resultVo.setLoginVo(loginVo == null ? new LoginVo(loginConfig.getCompanyCode()) : loginVo);
        return loginVo;
    }

    /**
     * 具体Login方法
     *
     * @param request
     * @param response
     * @param loginConfig
     * @return
     */
    protected abstract LoginVo doLogin(HttpServletRequest request, HttpServletResponse response, LoginConfig loginConfig);

    /**
     * 处理请求参数
     */
    private Map<String, Object> processInputMap(HttpServletRequest request) {
        return HttpRequestUtils.processInputMap(request);
    }

    /**
     * 验证ip地址
     *
     * @param request
     * @param loginConfig
     * @return
     */
    private boolean validateIpAddress(HttpServletRequest request, LoginConfig loginConfig) {
        String ip = getIpAddress(request);
        CommonConfig commonConfig = (CommonConfig) loginConfig;
        boolean result = SSOUtils.validateIpAddress(commonConfig.getCompanyIp(), ip);
        if (!result) {
            throw new SsoException(MessageSourceUtils.getMessage("sso.companyIpError.config"), loginConfig);
        }
        return result;
    }

    /**
     * 是否需要验证ip地址
     *
     * @param loginConfig
     * @return
     */
    boolean isNeedValidateIp(LoginConfig loginConfig) {
        if (StringUtils.isNotBlank(loginConfig.getValidateIp())) {
            return BooleanUtils.toBoolean(loginConfig.getValidateIp());
        }
        return false;
    }
}

package com.citsgbt.mobile.gateway.security.sso.common;

import com.citsamex.app.spi.data.caller.request.master.BaseCompanyParam;
import com.citsamex.app.spi.data.caller.response.company.CompanySsoConfigResponse;
import com.citsamex.app.spi.interfaces.targets.company.CompanyTargetService;
import com.citsgbt.mobile.core.utils.spring.MessageSourceUtils;
import com.citsgbt.mobile.gateway.security.sso.exception.SsoException;
import com.citsgbt.mobile.gateway.security.sso.utils.DateUtils;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginVo;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.Date;

/**
 * 外部传递jwttoken验证用户登陆
 */
@Component
public class JwtLogin extends AbstractLogin {

    private int timeToLive = 300;

    private int futureTimeToLive = 300;

    @Autowired
    private CompanyTargetService companyTargetService;

    @Override
    public LoginVo doLogin(HttpServletRequest request, HttpServletResponse response, LoginConfig loginConfig) {
        String tokenName = loginConfig.getTokenName();
        tokenName = StringUtils.isBlank(tokenName) ? SSOUtils.JWT_ACCESS_TOKEN_KEY : tokenName;
        String principal = request.getHeader(tokenName);
        if (StringUtils.isBlank(principal)) {
            principal = request.getParameter(tokenName);
        }
        if (StringUtils.isNotBlank(principal)) {
            // 解密参数
            logger.info("Token Login [{}]", principal);
            try {
                return calcLoginVo(loginConfig, principal);
            } catch (SsoException e) {
                throw e;
            } catch (Exception e) {
                throw new SsoException(MessageSourceUtils.getMessage("sso.invalid.no.principal"), e, loginConfig);
            }
        }
        return null;
    }

    protected LoginVo calcLoginVo(LoginConfig loginConfig, String principal) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(principal);
        JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
        String companyCode = jwtClaimsSet.getStringClaim("companyCode");
        String loginName = jwtClaimsSet.getStringClaim("loginName");
        Long timestamp = jwtClaimsSet.getLongClaim("timestamp");
        Date date = new Date(timestamp); // 校验时间戳的正确性
        if (DateUtils.verifyCreated(date, timeToLive, futureTimeToLive)) {
            BaseCompanyParam param = new BaseCompanyParam();
            param.setCompanyCode(companyCode);
            CompanySsoConfigResponse ssoConfigResponse = companyTargetService.loadCompanySsoConfig(param);
            String password = null;
            if (ssoConfigResponse.success() && ssoConfigResponse.getCompanySimpleAccount() != null) {
                password = ssoConfigResponse.getCompanySimpleAccount().getPassword();
            }
            JWSVerifier verifier = new MACVerifier(password);
            boolean result = signedJWT.verify(verifier);
            if (result) {
                return new LoginVo(companyCode, loginName);
            } else {
                logger.error("Token验证用户失败");
                throw new SsoException(MessageSourceUtils.getMessage("sso.sign.check.error"), loginConfig);
            }
        } else {
            logger.error("该单点登陆请求已经过期");
            throw new SsoException(MessageSourceUtils.getMessage("sso.invalid.assert.expired"), loginConfig);
        }
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public int getFutureTimeToLive() {
        return futureTimeToLive;
    }

    public void setFutureTimeToLive(int futureTimeToLive) {
        this.futureTimeToLive = futureTimeToLive;
    }
}

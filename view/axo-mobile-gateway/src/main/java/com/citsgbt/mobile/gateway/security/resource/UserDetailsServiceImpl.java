package com.citsgbt.mobile.gateway.security.resource;

import com.citsamex.app.spi.data.caller.request.profile.LoginParam;
import com.citsamex.app.spi.data.caller.response.profile.LoginResponse;
import com.citsamex.app.spi.interfaces.targets.profile.LoginTargetService;
import com.citsgbt.mobile.gateway.security.user.CustomUserDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private LoginTargetService loginTargetService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        String message = "input error";
        if (StringUtils.isNotBlank(username)) {
            String[] loginNames = username.split("\\s*,\\s*");
            if (loginNames.length == 2) {
                String companyCode = loginNames[0];
                String loginName = loginNames[1];
                LoginParam loginParam = new LoginParam();
                loginParam.setCompanyCode(companyCode);
                loginParam.setLoginName(loginName);
                LoginResponse response = loginTargetService.getUserByLoginName(loginParam, null);
                if (response != null) {
                    if (response.success() && response.getUserBasic() != null) {
                        CustomUserDetails customUserDetails = new CustomUserDetails(response.getUserBasic(), username,
                                response.getProductCodes(), response.getLoginCheck(), response.getNoticeMessage());
                        customUserDetails.setLoginName(loginName);
                        customUserDetails.setCompanyCode(companyCode);
                        return customUserDetails;
                    } else {
                        message = response.getResponseHead().getResultMessage();
                    }
                }
            }
        }
        throw new UsernameNotFoundException(message);
    }

}

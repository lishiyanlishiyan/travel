package com.citsgbt.mobile.gateway.security.sso.oauth;

import com.citsgbt.mobile.core.utils.http.HttpRequestUtils;
import com.citsgbt.mobile.core.web.vo.SsoParamsDto;
import com.citsgbt.mobile.core.ws.config.sso.SsoParamConfigProperties;
import com.citsgbt.mobile.gateway.security.sso.utils.Keys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * @author gary.fu
 */
public class CustomJwtAccessTokenConverter extends JwtAccessTokenConverter {

	private static final Logger logger = LoggerFactory.getLogger(CustomJwtAccessTokenConverter.class);

	private RedisTemplate redisTemplate;

	private SsoParamConfigProperties ssoParamConfigProperties;

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		OAuth2AccessToken oAuth2AccessToken = super.enhance(accessToken, authentication);
		HttpServletRequest request = HttpRequestUtils.getCurrentRequest();
		if (request != null && redisTemplate != null) {
			SsoParamsDto ssoParams = (SsoParamsDto) request.getSession().getAttribute(Keys.SSO_PARAMS_SAVE_KEY);
			if (ssoParams != null) {
				try {
					String token = oAuth2AccessToken.getValue();
					String key = StringUtils.join(ssoParamConfigProperties.getSsoParamPrefix(), token);
					redisTemplate.opsForValue().set(key, ssoParams, getExpiresIn(), TimeUnit.SECONDS);
				} catch (Exception e) {
					logger.error("存储到Redis错误", e);
				}
			}
		}
		return oAuth2AccessToken;
	}

	protected Integer getExpiresIn() {
		if (getSsoParamConfigProperties() != null) {
			return getSsoParamConfigProperties().getSsoParamExpire();
		}
		return 10 * 60;
	}

	public RedisTemplate getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public SsoParamConfigProperties getSsoParamConfigProperties() {
		return ssoParamConfigProperties;
	}

	public void setSsoParamConfigProperties(SsoParamConfigProperties ssoParamConfigProperties) {
		this.ssoParamConfigProperties = ssoParamConfigProperties;
	}
}

package com.citsgbt.mobile.app;

import com.citsgbt.mobile.core.config.index.DefaultIndexConfig;
import com.citsgbt.mobile.core.config.redis.DefaultRedisConfig;
import com.citsgbt.mobile.core.ws.config.appws.AppWsProxyConfig;
import com.citsgbt.mobile.core.ws.config.appws.AppWsWebServiceConfig;
import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2AutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Import;

@EnableSwagger2Doc
@SpringBootApplication(scanBasePackages = {"com.citsgbt.mobile"}, exclude = {OAuth2AutoConfiguration.class, SecurityAutoConfiguration.class})
@Import({DefaultIndexConfig.class, AppWsProxyConfig.class, AppWsWebServiceConfig.class, DefaultRedisConfig.class})
public class AxoMobileOpenApplication {

    public static void main(String[] args) {
        SpringApplication.run(AxoMobileOpenApplication.class, args);
    }
}

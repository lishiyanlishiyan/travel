package com.citsgbt.mobile.gateway;

import com.citsgbt.mobile.core.config.redis.DefaultRedisConfig;
import com.citsgbt.mobile.core.ws.config.appws.AppWsProxyConfig;
import com.citsgbt.mobile.core.ws.config.appws.AppWsWebServiceConfig;
import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Import;

@EnableZuulProxy
@EnableSwagger2Doc
@SpringBootApplication(scanBasePackages = {"com.citsgbt.mobile"})
@SpringCloudApplication
@Import({AppWsProxyConfig.class, AppWsWebServiceConfig.class, DefaultRedisConfig.class})
public class AxoGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AxoGatewayApplication.class, args);
    }
}

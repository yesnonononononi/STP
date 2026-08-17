package com.summit.stp.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(
    basePackages = {"com.summit.stp.common", "com.summit.stp.gateway"},
    excludeFilters = {
        @ComponentScan.Filter(
            type = org.springframework.context.annotation.FilterType.REGEX,
            pattern = "com\\.summit\\.stp\\.common\\.config\\.(SqlConfig|global\\.StpMetaObjectHandler)"
        )
    }
)
public class StpGatewayApplication {
    public static void main(String[] args) {

        SpringApplication.run(StpGatewayApplication.class, args);
    }
}

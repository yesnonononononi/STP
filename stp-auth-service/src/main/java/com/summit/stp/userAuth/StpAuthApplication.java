package com.summit.stp.userAuth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.summit.stp.common.feign"})
@ComponentScan(basePackages = {"com.summit.stp.userAuth", "com.summit.stp.common"})
@MapperScan("com.summit.stp.userAuth.infrastructure.persistence.mapper")
public class StpAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(StpAuthApplication.class, args);
    }
}

package com.summit.stp.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients(basePackages = "com.summit.stp.common.feign")
@ComponentScan(basePackages = {"com.summit.stp.common","com.summit.stp.order"})
@MapperScan("com.summit.stp.order.infrastructure.persistence.mapper")
public class StpOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(StpOrderApplication.class, args);
    }
}

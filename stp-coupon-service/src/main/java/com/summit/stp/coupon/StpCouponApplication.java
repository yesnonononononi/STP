package com.summit.stp.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.summit.stp.order.api","com.summit.stp.user.api"})
@ComponentScan(basePackages = {"com.summit.stp.coupon", "com.summit.stp.user_coupon", "com.summit.stp.activity", "com.summit.stp.common"})
@MapperScan({"com.summit.stp.coupon.infrastructure.persistence.mapper", "com.summit.stp.user_coupon.infrastructure.persistence.mapper", "com.summit.stp.activity.infrastructure.persistence.mapper"})
public class StpCouponApplication {
    public static void main(String[] args) {
        SpringApplication.run(StpCouponApplication.class, args);
    }
}

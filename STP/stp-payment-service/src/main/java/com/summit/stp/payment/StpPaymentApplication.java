package com.summit.stp.payment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.summit.stp.common","com.summit.stp.payment"})
@MapperScan("com.summit.stp.payment.infrastructure.persistence.mapper")
public class StpPaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(StpPaymentApplication.class, args);
    }
}

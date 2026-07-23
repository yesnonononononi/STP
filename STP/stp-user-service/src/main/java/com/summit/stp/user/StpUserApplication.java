package com.summit.stp.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.summit.stp.user", "com.summit.stp.member", "com.summit.stp.relationship", "com.summit.stp.shared"})
@MapperScan(basePackages = {
    "com.summit.stp.user.infrastructure.persistence.mapper",
    "com.summit.stp.member.infrastructure.persistence.mapper",
    "com.summit.stp.relationship.infrastructure.persistence.mapper"
})
public class StpUserApplication {
    public static void main(String[] args) {
        System.setProperty("nacos.server.grpc.port.offset", "1768");
        SpringApplication.run(StpUserApplication.class, args);
    }
}

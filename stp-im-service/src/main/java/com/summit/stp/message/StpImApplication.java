package com.summit.stp.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.summit.stp.user.api","com.summit.stp.comment.api","com.summit.stp.post.api"})
@ComponentScan(basePackages = {"com.summit.stp.message","com.summit.stp.message.admin", "com.summit.stp.message.entertainment", "com.summit.stp.common"})
@MapperScan(basePackages = {
    "com.summit.stp.message.message.infrastructure.persistence.mapper",
    "com.summit.stp.message.entertainment.infrastructure.persistence.mapper"
})
public class StpImApplication {
    public static void main(String[] args) {
        SpringApplication.run(StpImApplication.class, args);
    }
}

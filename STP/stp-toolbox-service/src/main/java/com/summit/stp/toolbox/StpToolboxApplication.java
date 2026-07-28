package com.summit.stp.toolbox;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.summit.stp.common","com.summit.stp.toolbox"})
@MapperScan("com.summit.stp.toolbox.infrastructure.persistence.mapper")
public class StpToolboxApplication {
    public static void main(String[] args) {
        SpringApplication.run(StpToolboxApplication.class, args);
    }
}

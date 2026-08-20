package com.summit.stp.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.summit")
@SpringBootApplication(scanBasePackages = "com.summit")
@MapperScan("com.summit.stp.admin.infrastructure.persistence.mapper")
public class StpAdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StpAdminServiceApplication.class, args);
    }

}

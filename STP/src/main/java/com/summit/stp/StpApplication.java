package com.summit.stp;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.summit.stp.**.mapper")
public class StpApplication {

    public static void main(String[] args) {
        SpringApplication.run(StpApplication.class, args);
    }

}

package com.summit.stp;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.summit.stp.**.mapper")
public class StpApplication {

    public static void main(String[] args) {
        SpringApplication.run(StpApplication.class, args);
    }

}

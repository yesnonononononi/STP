package com.summit.stp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients(basePackages = "com.summit.stp.user.api")
@EnableElasticsearchRepositories(basePackages = "com.summit.stp.elasticsearch.repo")
@ComponentScan(basePackages = {"com.summit.stp.post", "com.summit.stp.tag", "com.summit.stp.rank_board", "com.summit.stp.common", "com.summit.stp.elasticsearch", "com.summit.stp.admin"})


@MapperScan(basePackages = {
    "com.summit.stp.post.infrastructure.persistence.mapper",
    "com.summit.stp.tag.infrastructure.persistence.mapper",
    "com.summit.stp.rank_board.infrastructure.persistence.mapper"
})
public class StpPostApplication {
    public static void main(String[] args) {
        SpringApplication.run(StpPostApplication.class, args);
    }
}

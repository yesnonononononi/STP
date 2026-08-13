package com.summit.stp.post;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients(basePackages = "com.summit.stp.common.feign")
@EnableElasticsearchRepositories(basePackages = "com.summit.stp.elasticsearch.repo")
@ComponentScan(basePackages = {"com.summit.stp.post", "com.summit.stp.tag", "com.summit.stp.rank_board", "com.summit.stp.common", "com.summit.stp.elasticsearch"})


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

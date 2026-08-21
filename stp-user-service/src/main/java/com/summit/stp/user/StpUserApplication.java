package com.summit.stp.user;

import com.summit.stp.elasticsearch.document.UserDocument;
import com.summit.stp.elasticsearch.repo.AdminUserQueryRepository;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.infrastructure.persistence.UserRepositoryImpl;
import jakarta.annotation.PostConstruct;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
@EnableAspectJAutoProxy
@EnableElasticsearchRepositories(basePackages = "com.summit.stp.elasticsearch.repo")
@ComponentScan(basePackages = {"com.summit.stp.user", "com.summit.stp.member","com.summit.stp.common","com.summit.stp.admin","com.summit.stp.elasticsearch"})
@MapperScan(basePackages = {
    "com.summit.stp.user.infrastructure.persistence.mapper",
    "com.summit.stp.member.infrastructure.persistence.mapper",
    "com.summit.stp.admin.infrastructure.persistence.mapper",
})
public class StpUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(StpUserApplication.class, args);
    }
}

package com.summit.stp.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("STP 项目在线接口文档")
                        .version("1.0.0")
                        .description("STP 社交系统的 API 调试与在线文档页面")
                        .contact(new Contact().name("Summit").email("summit@example.com")));
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("1. 用户模块")
                .pathsToMatch("/user/**")
                .build();
    }

    @Bean
    public GroupedOpenApi postApi() {
        return GroupedOpenApi.builder()
                .group("2. 帖子与标签模块")
                .pathsToMatch("/post/**")
                .build();
    }

    @Bean
    public GroupedOpenApi couponApi() {
        return GroupedOpenApi.builder()
                .group("3. 优惠券模块")
                .pathsToMatch("/coupon/**")
                .build();
    }

    @Bean
    public GroupedOpenApi orderApi() {
        return GroupedOpenApi.builder()
                .group("4. 订单模块")
                .pathsToMatch("/order/**")
                .build();
    }

    @Bean
    public GroupedOpenApi paymentApi() {
        return GroupedOpenApi.builder()
                .group("5. 支付模块")
                .pathsToMatch("/pay/**", "/payment/**")
                .build();
    }

    @Bean
    public GroupedOpenApi memberApi() {
        return GroupedOpenApi.builder()
                .group("6. 会员模块")
                .pathsToMatch("/member/**")
                .build();
    }
}

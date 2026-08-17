package com.summit.stp.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.summit.stp.common.config.global.Interceptor;
import com.summit.stp.common.resolver.PublicIdArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.util.List;

/**
 * 公共 Web 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final Interceptor authInterceptor;
    private final RequestLoggingInterceptor requestLoggingInterceptor;
    private final PublicIdArgumentResolver publicIdArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(publicIdArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加请求日志拦截器（最先执行）
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/error", "/favicon.ico");

        // 添加统一鉴权与 Session 反序列化拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user-auth/login",
                        "/user-auth/refresh-token",
                        "/user-auth/register",
                        "/user-auth/forget",
                        "/pay/check",
                        "/u/admin/internal/is/**",
                        "/static/**",
                        "/error"
                );
    }
}

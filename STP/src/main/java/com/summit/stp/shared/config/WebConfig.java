package com.summit.stp.shared.config;

import com.summit.stp.shared.handler.Interceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final Interceptor authInterceptor;
    private final RequestLoggingInterceptor requestLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加请求日志拦截器（最先执行）
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/error", "/favicon.ico");
        
        // 添加鉴权拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user-auth/login","/pay/check", "/user-auth/refresh-token","/user-auth/register","/user-auth/forget", "/static/**", "/error");
    }
}

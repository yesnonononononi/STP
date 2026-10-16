package com.summit.stp.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, @NonNull Object handler) {
        // 脱敏：仅记录请求方法 + 路径（getRequestURI 不含 query），
        // 避免将带敏感参数（token / code / 手机号等）的完整 URL 写入日志。
        log.info("请求: {} {}", request.getMethod(), request.getRequestURI());
        return true;
    }
}

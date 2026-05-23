package com.summit.stp.shared.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;
import java.util.Map;

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("========== 收到请求 ==========");
        log.info("请求方法: {}", request.getMethod());
        log.info("请求URL: {}", request.getRequestURL());
        log.info("请求URI: {}", request.getRequestURI());
        log.info("远程地址: {}", request.getRemoteAddr());
        log.info("Content-Type: {}", request.getContentType());

        // 打印所有请求头
        log.info("请求头:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.info("  {} = {}", headerName, request.getHeader(headerName));
        }

        // 打印所有请求参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (!parameterMap.isEmpty()) {
            log.info("请求参数:");
            parameterMap.forEach((key, values) -> {
                for (String value : values) {
                    log.info("  {} = {}", key, value);
                }
            });
        }

        log.info("================================");
        return true;
    }
}

package com.summit.stp.gateway.filter;

import com.summit.stp.userAuth.infrastructure.constants.UserAuthConstants;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.json.JsonMapper;
import com.summit.stp.userAuth.domain.model.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizeGatewayFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    private final JsonMapper objectMapper;

    @Override
    public @NonNull Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 认证相关接口直接放行，由 auth 服务自主完成登录与 Token 生成
        if (path.contains("/user-auth/login") || 
            path.contains("/user-auth/register") || 
            path.contains("/user-auth/refresh-token") || 
            path.contains("/user-auth/forget")) {
            return chain.filter(exchange);
        }


        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String key = UserAuthConstants.Cache.ACCESS_SESSION + token;
            
            return reactiveStringRedisTemplate.opsForValue().get(key)
                    .flatMap(json -> {
                        try {
                            UserSession session = objectMapper.readValue(json, UserSession.class);
                            if (session != null) {
                                ServerHttpRequest.Builder builder = request.mutate()
                                        .header("X-User-Id", String.valueOf(session.getId()))
                                        .header("X-User-Name", session.getUsername() != null ? session.getUsername() : "")
                                        .header("X-User-Admin", session.getAdmin() != null ? String.valueOf(session.getAdmin()) : "0")
                                        .header("X-User-Token-Type", session.getTokenType() != null ? session.getTokenType().name() : "")
                                        .header("X-User-Token", session.getToken() != null ? session.getToken() : "");
                                return chain.filter(exchange.mutate().request(builder.build()).build());
                            }
                        } catch (Exception e) {
                            log.error("【网关】解析用户会话 Session 异常", e);
                        }
                        return chain.filter(exchange);
                    })
                    .switchIfEmpty(Mono.defer(() -> chain.filter(exchange)))
                    .onErrorResume(e -> {
                        log.error("【网关】读取 Token 发生异常", e);
                        return chain.filter(exchange);
                    });
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100; // 最先执行
    }
}

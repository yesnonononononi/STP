package com.summit.stp.gateway.filter;

import cn.hutool.core.util.StrUtil;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.application.domain.repository.JwtRepository;
import com.summit.stp.common.constants.UserAuthConstants;
import com.summit.stp.common.application.api.result.Result;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Slf4j
@Component

public class AuthorizeGatewayFilter implements GlobalFilter, Ordered {

    @Autowired
    private JsonMapper objectMapper;
    @Autowired(required = false)
    private JwtRepository jwtRepository;
    final String TOKEN_INVALID = "令牌无效,请重新尝试登录";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public @NonNull Mono<Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
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

            Object val = stringRedisTemplate.opsForValue().get(key);
            if (val == null) {
                return unauthorized(exchange, TOKEN_INVALID);
            }
            try {
                UserSession session = objectMapper.readValue(val.toString(), UserSession.class);
                if (session == null) return unauthorized(exchange, TOKEN_INVALID);
                return putUser(session, request, chain, exchange);

            } catch (Exception e) {
                log.error("【网关】解析用户会话 Session 异常", e);
                return unauthorized(exchange, TOKEN_INVALID);
            }

        } else if (authHeader != null && authHeader.startsWith("fb_")) {
            // jwt token
            String token = authHeader.substring(3);
            Result<UserSession> res = validateToken(token);
            if (res.isSuccess()) {
                return putUser(res.getData(), request, chain, exchange);
            } else {
                // 认证失败，直接返回 401
                return unauthorized(exchange, StrUtil.isEmptyIfStr(res.getErrMsg()) ? TOKEN_INVALID : res.getErrMsg());
            }
        }
        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String errMsg) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) return Mono.empty();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response
                .bufferFactory()
                .wrap(errMsg.getBytes(StandardCharsets.UTF_8));
        // 返回响应并结束，不再继续调用 chain.filter
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> putUser(@NonNull UserSession session, @NonNull ServerHttpRequest request, @NonNull GatewayFilterChain chain, @NonNull ServerWebExchange exchange) {
        ServerHttpRequest.Builder builder = request.mutate()
                .header("X-User-Id", String.valueOf(session.getId()))
                .header("X-User-Name", Objects.toString(session.getUsername(), ""))
                .header("X-User-Admin", Objects.toString(session.getAdmin(), ""))
                .header("X-User-Token-Type", Objects.toString(session.getTokenType(), ""))
                .header("X-User-Token", Objects.toString(session.getToken(), ""));
        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    private Result<UserSession> validateToken(String token) {
        try {
            // 1. 核心解析：只要这行抛出异常，后面的代码全都不执行
            UserSession us = jwtRepository.parseToken(token);
            return Result.success(us);
        } catch (ExpiredJwtException e) {
            return Result.error("令牌已过期,请重新尝试登录");

        } catch (JwtException e) {
            // 捕获所有其他 JWT 异常（签名错误、格式错误等）
            return Result.error("令牌无效,请重新尝试登录");

        } catch (RuntimeException e) {
            // 捕获你手动抛出的业务异常（比如版本号不匹配）
            return Result.error("解析令牌错误,请重新尝试登录");
        }
    }


    @Override
    public int getOrder() {
        return -100; // 最先执行
    }
}

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
import java.util.List;
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

    /** 内部 RPC 接口路径标识，命中即拒绝外部访问 */
    private static final String INTERNAL_PATH_SEGMENT = "/internal/";

    /**
     * 客户端可伪造的透传头。
     * 必须在鉴权之前无条件剥离，随后由网关基于「校验通过的 token」重新注入，
     * 确保匿名 / 无 token 请求也不能携带伪造身份头到达下游服务。
     */
    private static final List<String> FORGED_CLIENT_HEADERS = List.of(
            "X-User-Id",
            "X-User-Name",
            "X-User-Admin",
            "X-User-IP",
            "X-User-Token",
            "X-User-Token-Type",
            "X-User-Session",
            "X-Internal-Request"
    );

    @Override
    public @NonNull Mono<Void> filter(ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        // 0. 安全基线：先剥离客户端伪造的透传头（剥离 → 鉴权 → 重新注入）
        ServerWebExchange sanitized = stripForgedHeaders(exchange);
        ServerHttpRequest request = sanitized.getRequest();
        String path = request.getURI().getPath();

        // 1. 内部 RPC 接口兜底拦截（代码层强约束，不依赖 Nacos 路由配置）：
        //    任何外部请求（含无 token 的匿名请求）访问 /**/internal/** 一律 403
        if (path.contains(INTERNAL_PATH_SEGMENT)) {
            log.warn("【网关】拦截外部访问内部接口，path={}, remote={}", path, request.getRemoteAddress());
            return forbidden(sanitized);
        }

        // 2. 认证相关接口直接放行，由 auth 服务自主完成登录与 Token 生成
        if (path.contains("/user-auth/login") ||
                path.contains("/user-auth/register") ||
                path.contains("/user-auth/refresh-token") ||
                path.contains("/user-auth/forget")) {
            return chain.filter(sanitized);
        }
        // 获取 token
        String authHeader = request.getHeaders().getFirst("Authorization");
        // 解析
        return resolveToken(authHeader, sanitized, chain);
    }

    /**
     * 剥离客户端传入的全部 X-User-* / X-Internal-Request 头。
     * HttpHeaders 为大小写不敏感容器，remove 可覆盖任意大小写变体。
     */
    private ServerWebExchange stripForgedHeaders(ServerWebExchange exchange) {
        ServerHttpRequest sanitizedRequest = exchange.getRequest().mutate()
                .headers(headers -> FORGED_CLIENT_HEADERS.forEach(headers::remove))
                .build();
        return exchange.mutate().request(sanitizedRequest).build();
    }

    /**
     * 返回 403，用于拦截内部接口的外部访问。
     */
    private Mono<Void> forbidden(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) return Mono.empty();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response.bufferFactory()
                .wrap("禁止外部访问内部接口".getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * resolve user Authentication token
     */
    private Mono<Void> resolveToken(String authHeader, ServerWebExchange exchange, GatewayFilterChain chain) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return onToken(authHeader, exchange, chain);

        } else if (authHeader != null && authHeader.startsWith("fb_")) {
            return onJwt(authHeader, exchange, chain);
        }
        return chain.filter(exchange);
    }

    /**
     * resolve Authentication token if token
     */
    private Mono<Void> onToken(String authHeader, ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
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
            throw e;
        }
    }

    /**
     * resolve Authentication token if jwt
     */
    private Mono<Void> onJwt(String authHeader, ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
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


    /**
     * 未登录
     */
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


    /**
     * put userinfo
     */
    private Mono<Void> putUser(@NonNull UserSession session, @NonNull ServerHttpRequest request, @NonNull GatewayFilterChain chain, @NonNull ServerWebExchange exchange) {
        Long id = session.getId();
        if(id == null)return unauthorized(exchange,"未找到用户信息");
        ServerHttpRequest.Builder builder = request.mutate()
                .header("X-User-Id", String.valueOf(id))
                .header("X-User-Name", Objects.toString(session.getUsername(), ""))
                .header("X-User-Admin", Objects.toString(session.getAdmin(), ""))
                .header("X-User-IP",Objects.toString(session.getIp(),""))
                .header("X-User-Token-Type", Objects.toString(session.getTokenType(), ""))
                .header("X-User-Token", Objects.toString(session.getToken(), ""));
        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    /**
     * resolve jwt-token && validate it
     *
     * @param token jwt
     * @return res
     */
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

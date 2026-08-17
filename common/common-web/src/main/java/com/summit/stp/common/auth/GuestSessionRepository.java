package com.summit.stp.common.auth;

import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.constants.UserAuthConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Optional;

/**
 * 游客会话存储。
 *
 * 该能力属于公共鉴权基础设施，不能依赖 auth-service 中的 TokenRepository，
 * 否则 common 模块会产生反向依赖，导致认证拦截器无法编译或启动。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuestSessionRepository {

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper objectMapper;

    public Optional<UserSession> findByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String json = redisTemplate.opsForValue().get(sessionKey(token));
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(objectMapper.readValue(json, UserSession.class));
        } catch (Exception e) {
            log.warn("【游客会话】解析 Redis 会话失败，token: {}", token, e);
            return Optional.empty();
        }
    }

    /**
     * 仅在 token 尚不存在时创建游客会话。
     *
     * 使用 Redis SET NX 保证多实例并发请求下同一个设备只会成功创建一次，
     * 避免“先查询、后写入”带来的重复创建和互相覆盖。
     */
    public Optional<UserSession> saveIfAbsent(String token, UserSession session) {
        if (token == null || token.isBlank() || session == null) {
            return Optional.empty();
        }

        try {
            boolean created = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
                    sessionKey(token),
                    objectMapper.writeValueAsString(session),
                    Duration.ofMillis(UserAuthConstants.Business.GUEST_TOKEN_TTL)
            ));
            return created ? Optional.of(session) : findByToken(token);
        } catch (Exception e) {
            log.warn("【游客会话】写入 Redis 失败，降级为当前请求会话，token: {}", token, e);
            return Optional.empty();
        }
    }

    private String sessionKey(String token) {
        return UserAuthConstants.Cache.GUEST_SESSION + token;
    }
}

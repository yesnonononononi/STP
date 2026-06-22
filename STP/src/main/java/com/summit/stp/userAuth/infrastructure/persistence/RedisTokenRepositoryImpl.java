package com.summit.stp.userAuth.infrastructure.persistence;


import com.summit.stp.shared.constants.RedisConstants;
import com.summit.stp.userAuth.domain.model.UserSession;
import com.summit.stp.userAuth.domain.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisTokenRepositoryImpl implements TokenRepository {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void saveSession(String token, UserSession session, long expireSeconds, String type) {
        String key = "access".equals(type) ? RedisConstants.Auth.ACCESS_SESSION + token 
                                            : RedisConstants.Auth.REFRESH_SESSION + token;
        try {
            String json = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(key, json, expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize session", e);
        }
    }

    @Override
    public Optional<UserSession> findSessionByToken(String token, String type) {
        String key = "access".equals(type) ? RedisConstants.Auth.ACCESS_SESSION + token 
                                            : RedisConstants.Auth.REFRESH_SESSION + token;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, UserSession.class));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize session", e);
        }
    }

    @Override
    public void saveUserToken(String username, String token, long expireSeconds, String type) {
        String key = "access".equals(type) ? RedisConstants.Auth.ACCESS_TOKEN + username 
                                            : RedisConstants.Auth.REFRESH_TOKEN + username;
        redisTemplate.opsForValue().set(key, token, expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Optional<String> findTokenByUsername(String username, String type) {
        String key = "access".equals(type) ? RedisConstants.Auth.ACCESS_TOKEN + username 
                                            : RedisConstants.Auth.REFRESH_TOKEN + username;
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    @Override
    public void deleteByUsername(String username, String type) {
        Optional<String> tokenOpt = findTokenByUsername(username, type);
        tokenOpt.ifPresent(token -> deleteByToken(token, type));
        String key = "access".equals(type) ? RedisConstants.Auth.ACCESS_TOKEN + username 
                                            : RedisConstants.Auth.REFRESH_TOKEN + username;
        redisTemplate.delete(key);
    }

    @Override
    public void deleteByToken(String token, String type) {
        String key = "access".equals(type) ? RedisConstants.Auth.ACCESS_SESSION + token 
                                            : RedisConstants.Auth.REFRESH_SESSION + token;
        redisTemplate.delete(key);
    }
}

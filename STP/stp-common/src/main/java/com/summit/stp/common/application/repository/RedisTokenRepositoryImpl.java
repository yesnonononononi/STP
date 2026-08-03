package com.summit.stp.common.application.repository;

import tools.jackson.databind.json.JsonMapper;
import com.summit.stp.common.constants.UserAuthConstants;
import com.summit.stp.common.application.domain.model.UserSession;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisTokenRepositoryImpl implements TokenRepository {
    private final StringRedisTemplate redisTemplate;
    private final JsonMapper objectMapper;

    public RedisTokenRepositoryImpl(StringRedisTemplate redisTemplate, JsonMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveSession(String token, UserSession session, long expireSeconds,  UserSession.TokenType  type) {
        String key = getSessionKey(token, type);
        try {
            String json = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(key, json, expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize session", e);
        }
    }

    @Override
    public Optional<UserSession> findSessionByToken(String token,  UserSession.TokenType  type) {
        String key = getSessionKey(token, type);
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
    public void saveUserToken(String username, String token, long expireSeconds,  UserSession.TokenType  type) {
        String key = getTokenKey(username, type);
        redisTemplate.opsForValue().set(key, token, expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Optional<String> findTokenByUsername(String username,  UserSession.TokenType  type) {
        String key = getTokenKey(username, type);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    @Override
    public void deleteByUsername(String username,  UserSession.TokenType  type) {
        Optional<String> tokenOpt = findTokenByUsername(username, type);
        tokenOpt.ifPresent(token -> deleteByToken(token, type));
        String key = getTokenKey(username, type);
        redisTemplate.delete(key);
    }

    @Override
    public void deleteByToken(String token, UserSession.TokenType type) {
        String key = getTokenKey(token, type);
        redisTemplate.delete(key);
    }

    private String getSessionKey(String token,  UserSession.TokenType  type) {
        if (type.equals(UserSession.TokenType.ACCESS)) {
            return UserAuthConstants.Cache.ACCESS_SESSION + token;
        } else if (type.equals(UserSession.TokenType.REFRESH)) {
            return UserAuthConstants.Cache.REFRESH_SESSION + token;
        } else if(type.equals(UserSession.TokenType.GUEST)){
            return UserAuthConstants.Cache.GUEST_SESSION + token;
        }
        throw new RuntimeException("Invalid token type");
    }

    private String getTokenKey(String username,  UserSession.TokenType  type) {
        if (type.equals(UserSession.TokenType.ACCESS)) {
            return UserAuthConstants.Cache.ACCESS_TOKEN + username;
        } else if (type.equals(UserSession.TokenType.REFRESH)) {
            return UserAuthConstants.Cache.REFRESH_TOKEN + username;
        } else if(type.equals(UserSession.TokenType.GUEST)) {
            return UserAuthConstants.Cache.GUEST_TOKEN + username;
        }
        throw new RuntimeException("Invalid token type");
    }
}

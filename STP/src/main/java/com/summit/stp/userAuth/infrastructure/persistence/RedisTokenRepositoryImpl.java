package com.summit.stp.userAuth.infrastructure.persistence;


import com.summit.stp.shared.constant.UserAuthConstants;
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
    public void saveSession(String token, UserSession session, long expireSeconds) {
        String key = UserAuthConstants.SESSION_CACHE_PREFIX + token;
        try {
            String json = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(key, json, expireSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize session", e);
        }
    }

    @Override
    public Optional<UserSession> findSessionByToken(String token) {
        String key = UserAuthConstants.SESSION_CACHE_PREFIX + token;
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
    public void saveUserToken(String username, String token, long expireSeconds) {
        String key = UserAuthConstants.TOKEN_CACHE_PREFIX + username;
        redisTemplate.opsForValue().set(key, token, expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Optional<String> findTokenByUsername(String username) {
        String key = UserAuthConstants.TOKEN_CACHE_PREFIX + username;
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    @Override
    public void deleteByUsername(String username) {
        Optional<String> tokenOpt = findTokenByUsername(username);
        tokenOpt.ifPresent(this::deleteByToken);
        String key = UserAuthConstants.TOKEN_CACHE_PREFIX + username;
        redisTemplate.delete(key);
    }

    @Override
    public void deleteByToken(String token) {
        String key = UserAuthConstants.SESSION_CACHE_PREFIX + token;
        redisTemplate.delete(key);
    }
}

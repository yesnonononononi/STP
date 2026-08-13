package com.summit.stp.userAuth.infrastructure.persistence;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.application.domain.repository.JwtRepository;
import com.summit.stp.common.constants.UserAuthConstants;
import com.summit.stp.userAuth.domain.repository.TokenRepository;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class RedisTokenRepositoryImpl implements TokenRepository {
    private final StringRedisTemplate redisTemplate;
    private final JsonMapper objectMapper;

    private static final DefaultRedisScript<Long> RESET_SCRIPT = new DefaultRedisScript<>();
    private static final DefaultRedisScript<Long> REFRESH_SCRIPT = new DefaultRedisScript<>();
    private final JwtRepository jwtRepository;


    public RedisTokenRepositoryImpl(StringRedisTemplate redisTemplate, JsonMapper objectMapper, JwtRepository jwtRepository) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        RESET_SCRIPT.setLocation(new ClassPathResource("lua/ResetUserInfo.lua"));
        REFRESH_SCRIPT.setLocation(new ClassPathResource("lua/RefreshToken.lua"));
        RESET_SCRIPT.setResultType(Long.class);
        REFRESH_SCRIPT.setResultType(Long.class);
        this.jwtRepository = jwtRepository;
    }


    @Override
    public Map.Entry<String, String> generateToken(@NonNull UserSession userSession) {
        // 1, 生成token
        String username = userSession.getUsername();
        if (StrUtil.isBlank(username)) throw new BusinessException("用户名不能为空");
        try {
            // 2, 删除所有refresh-token access-token 关联
            return resetAuthUserInfo(userSession, null, null);
        } catch (Exception e) {
            log.error("【登录】Redis 连接异常, 降级为JWT方案 username:{}", username, e);
            //jwt 方案
            return Map.entry(fallbackOfJwt(userSession), UserAuthConstants.Business.JWT_FALLBACK);
        }
    }

    @Override
    public Map.Entry<String, String> resetAuthUserInfo(UserSession userSession, @Nullable String oldAccessToken, @Nullable String oldRefreshToken) {
        String newToken = IdUtil.fastSimpleUUID();
        String newRefreshToken = "r_" + IdUtil.fastSimpleUUID();
        Map.Entry<String, String> entry = Map.entry(newToken, newRefreshToken);
        String username = userSession.getUsername();
        List<String> keyList = List.of(
                getSessionKey(oldAccessToken, UserSession.TokenType.ACCESS),   // KEYS[1]
                getSessionKey(oldRefreshToken, UserSession.TokenType.REFRESH), // KEYS[2]
                getTokenKey(username, UserSession.TokenType.ACCESS),           // KEYS[3]
                getTokenKey(username, UserSession.TokenType.REFRESH),          // KEYS[4]
                getSessionKey(newToken, UserSession.TokenType.ACCESS),   // KEYS[5]
                getSessionKey(newRefreshToken, UserSession.TokenType.REFRESH)  // KEYS[6]
        );

        Object[] args = new Object[]{
                String.valueOf(UserAuthConstants.Business.DEFAULT_TOKEN_EXPIRE_SECONDS),
                newToken,
                String.valueOf(UserAuthConstants.Business.DEFAULT_REFRESH_TOKEN_EXPIRE_SECONDS),
                newRefreshToken,
                objectMapper.writeValueAsString(userSession)
        };


        Long execute = redisTemplate.execute(RESET_SCRIPT, keyList, args);
        if (execute != 1) {
            throw new RuntimeException("Failed to execute Lua script");
        }

        return entry;
    }

    @Override
    public void logout(String oldAccessToken, String oldRefreshToken, String username) {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(oldAccessToken) || StrUtil.isBlank(oldRefreshToken)) {
            throw new IllegalArgumentException("认证信息不全,无法执行登出");
        }
        List<String> keyList = List.of(
                getSessionKey(oldAccessToken, UserSession.TokenType.ACCESS),   // KEYS[1]
                getSessionKey(oldRefreshToken, UserSession.TokenType.REFRESH), // KEYS[2]
                getTokenKey(username, UserSession.TokenType.ACCESS),           // KEYS[3]
                getTokenKey(username, UserSession.TokenType.REFRESH),          // KEYS[4]
                "",   // KEYS[5]
                ""  // KEYS[6]
        );
        Object[] args = new Object[]{
                String.valueOf(UserAuthConstants.Business.DEFAULT_TOKEN_EXPIRE_SECONDS),
                "",
                String.valueOf(UserAuthConstants.Business.DEFAULT_REFRESH_TOKEN_EXPIRE_SECONDS),
                "",
                ""};
        if (redisTemplate.execute(RESET_SCRIPT, keyList, args) != 1) {
            throw new RuntimeException("Failed to execute Lua script");
        }
    }


    private @NonNull String fallbackOfJwt(UserSession us) {
        return "fb_" + jwtRepository.generateToken(us);
    }


    @Override
    public Optional<UserSession> findSessionByToken(String token, UserSession.TokenType type) {
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
    public boolean refreshToken(String oldAccessToken, String refreshToken, String newAccessToken, UserSession userSession) {
        String username = userSession.getUsername();
        List<String> keys = List.of(
                getTokenKey(username, UserSession.TokenType.ACCESS),
                getSessionKey(oldAccessToken, UserSession.TokenType.ACCESS),
                getTokenKey(username, UserSession.TokenType.REFRESH),
                getSessionKey(refreshToken, UserSession.TokenType.REFRESH),
                getSessionKey(newAccessToken, UserSession.TokenType.ACCESS)
        );

        Object[] args = new Object[]{
                String.valueOf(UserAuthConstants.Business.DEFAULT_TOKEN_EXPIRE_SECONDS),
                String.valueOf(UserAuthConstants.Business.DEFAULT_REFRESH_TOKEN_EXPIRE_SECONDS),
                refreshToken,
                objectMapper.writeValueAsString(userSession),
                newAccessToken
        };
        try {
            return (redisTemplate.execute(REFRESH_SCRIPT, keys, args) == 1);
        } catch (Exception e) {
            log.error("【登录】Redis 刷新token异常 username:{}", username, e);
            return false;
        }
    }


    private String getSessionKey(String token, UserSession.TokenType type) {
        if (type.equals(UserSession.TokenType.ACCESS)) {
            return UserAuthConstants.Cache.ACCESS_SESSION + token;
        } else if (type.equals(UserSession.TokenType.REFRESH)) {
            return UserAuthConstants.Cache.REFRESH_SESSION + token;
        } else if (type.equals(UserSession.TokenType.GUEST)) {
            return UserAuthConstants.Cache.GUEST_SESSION + token;
        }
        throw new RuntimeException("Invalid token type");
    }

    private String getTokenKey(String username, UserSession.TokenType type) {
        if (type.equals(UserSession.TokenType.ACCESS)) {
            return UserAuthConstants.Cache.ACCESS_TOKEN + username;
        } else if (type.equals(UserSession.TokenType.REFRESH)) {
            return UserAuthConstants.Cache.REFRESH_TOKEN + username;
        } else if (type.equals(UserSession.TokenType.GUEST)) {
            return UserAuthConstants.Cache.GUEST_TOKEN + username;
        }
        throw new RuntimeException("Invalid token type");
    }
}



package com.summit.stp.userAuth.domain.repository;

import com.summit.stp.common.application.domain.model.UserSession;
import jakarta.annotation.Nullable;
import lombok.NonNull;

import java.util.Map;
import java.util.Optional;

/**
 * 用户认证令牌仓储。
 *
 * 令牌的生成、轮换和删除必须由同一个仓储统一维护，避免登录服务和 Redis
 * 实现各自拼装缓存 key，导致 access/refresh session 不一致。
 */
public interface TokenRepository {


    Map.Entry<String, String> generateToken(@NonNull UserSession userSession);

    Map.Entry<String, String> resetAuthUserInfo(UserSession userSession, @Nullable String oldAccessToken, @Nullable String oldRefreshToken);

    void logout(String oldAccessToken, String oldRefreshToken, String username);

    Optional<UserSession> findSessionByToken(String token, UserSession.TokenType type);

    boolean refreshToken(String oldAccessToken, String refreshToken, String newAccessToken, UserSession userSession);
}

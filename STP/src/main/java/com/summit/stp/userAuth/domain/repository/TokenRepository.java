package com.summit.stp.userAuth.domain.repository;

import com.summit.stp.userAuth.domain.model.UserSession;

import java.util.Optional;

public interface TokenRepository {
    /**
     * 保存 Session 映射 (token -> UserSession)
     */
    void saveSession(String token, UserSession session, long expireSeconds, String type);

    /**
     * 根据 Token 获取 Session 信息
     */
    Optional<UserSession> findSessionByToken(String token, String type);

    /**
     * 保存 用户 -> Token 映射 (username -> token) 用于单点登录或查看状态
     */
    void saveUserToken(String username, String token, long expireSeconds, String type);

    /**
     * 根据用户名获取 Token
     */
    Optional<String> findTokenByUsername(String username, String type);

    /**
     * 删除 Token 及相关 Session
     */
    void deleteByUsername(String username, String type);
    
    /**
     * 根据 Token 删除 Session
     */
    void deleteByToken(String token, String type);
}

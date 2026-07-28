package com.summit.stp.common.application.repository;

import com.summit.stp.common.application.domain.model.UserSession;

import java.util.Optional;

public interface TokenRepository {
    /**
     * 保存 Session 映射 (token -> UserSession)
     */
    void saveSession(String token, UserSession session, long expireSeconds,  UserSession.TokenType  type);

    /**
     * 根据 Token 获取 Session 信息
     */
    Optional<UserSession> findSessionByToken(String token,  UserSession.TokenType  type);

    /**
     * 保存 用户 -> Token 映射 (username -> token) 用于单点登录或查看状态
     */
    void saveUserToken(String username, String token, long expireSeconds,  UserSession.TokenType  type);

    /**
     * 根据用户名获取 Token
     */
    Optional<String> findTokenByUsername(String username,  UserSession.TokenType     type);

    /**
     * 删除 Token 及相关 Session
     */
    void deleteByUsername(String username,  UserSession.TokenType  type);
    
    /**
     * 根据 Token 删除 Session
     */
    void deleteByToken(String token,  UserSession.TokenType  type);
}

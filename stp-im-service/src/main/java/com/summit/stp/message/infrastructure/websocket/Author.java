package com.summit.stp.message.infrastructure.websocket;

import com.corundumstudio.socketio.*;
import com.summit.stp.message.infrastructure.websocket.provide.CacheProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Author implements AuthorizationListener, AuthTokenListener {
    private final CacheProvider cacheProvider;
    public static final String TOKEN = "token";

    public Author(CacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    @Override
    public AuthorizationResult getAuthorizationResult(HandshakeData data) {
        String token = data.getSingleUrlParam(TOKEN);
        log.info("【Socket.IO】收到客户端握手请求，地址：{}", data.getAddress());
        if (token == null) {
            log.warn("【Socket.IO】握手鉴权失败：authToken 为空");
            return AuthorizationResult.FAILED_AUTHORIZATION;
        }
        
        if (cacheProvider.auth(token)) {
            log.info("【Socket.IO】握手鉴权成功！");
            return AuthorizationResult.SUCCESSFUL_AUTHORIZATION;
        }
        
        log.warn("【Socket.IO】握手鉴权失败：token 校验未通过，token: {}", token);
        return AuthorizationResult.FAILED_AUTHORIZATION;
    }

    @Override
    public AuthTokenResult getAuthTokenResult(Object authToken, SocketIOClient client) {
        return null;
    }
}

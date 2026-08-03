package com.summit.stp.common.infrastructure.websocket.listener;

import com.corundumstudio.socketio.SocketIOClient;
import com.summit.stp.common.infrastructure.websocket.Connector;
import com.summit.stp.common.infrastructure.websocket.provide.CacheProvider;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DisconnectListener implements com.corundumstudio.socketio.listener.DisconnectListener {
    private final CacheProvider cacheProvider;
    private final RedissonClient wsRedissonClient;

    public DisconnectListener(CacheProvider cacheProvider, @Qualifier("wsRedissonClient") RedissonClient wsRedissonClient) {
        this.cacheProvider = cacheProvider;
        this.wsRedissonClient = wsRedissonClient;
    }

    @Override
    public void onDisconnect(SocketIOClient client) {
        String uidStr = client.get(Connector.UID);
        if (uidStr != null && !uidStr.isEmpty()) {
            try {
                Long uid = Long.valueOf(uidStr);
                cacheProvider.removeOnline(uid, client.getSessionId().toString());
            } catch (NumberFormatException e) {
                log.warn("【客户端断开】UID格式非法: {}", uidStr);
            }
        }

        // 主动物理清除 Redisson 底层存放的客户端连接元数据 Map 缓存
        try {
            wsRedissonClient.getMap(client.getSessionId().toString()).delete();
            log.info("【WebSocket】已主动清理断开客户端的 Redis 缓存: {}", client.getSessionId());
        } catch (Exception e) {
            log.warn("【WebSocket】主动清理客户端 Redis 缓存失败: {}", client.getSessionId(), e);
        }

        client.disconnect();

        log.info("【客户端断开连接】UID: {}, SessionId: {}", uidStr, client.getSessionId());
    }
}

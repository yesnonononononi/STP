package com.summit.stp.message.infrastructure.websocket.listener;

import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.summit.stp.message.infrastructure.websocket.Connector;
import com.summit.stp.message.infrastructure.websocket.provide.CacheProvider;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ConnectListener implements com.corundumstudio.socketio.listener.ConnectListener {
    private final CacheProvider cacheProvider;
    private final RedissonClient wsRedissonClient;

    public ConnectListener(CacheProvider cacheProvider, @Qualifier("wsRedissonClient") RedissonClient wsRedissonClient) {
        this.cacheProvider = cacheProvider;
        this.wsRedissonClient = wsRedissonClient;
    }

    @Override
    public void onConnect(SocketIOClient client) {
        log.info("【WebSocket】客户端开始建立连接: {}", client.getSessionId());
        HandshakeData handshakeData = client.getHandshakeData();
        String uid = handshakeData.getSingleUrlParam(Connector.UID);
        if (uid == null || uid.isEmpty()) {
            log.warn("【WebSocket】客户端连接非法UID被阻断: {}", uid);
            client.disconnect();
            return;
        }

        long uidLong;
        try {
            uidLong = Long.parseLong(uid);
        } catch (NumberFormatException e) {
            log.warn("【WebSocket】客户端连接UID数字转换失败: {}", uid);
            client.disconnect();
            return;
        }

        client.set(Connector.UID, uid);
        
        // 写入轻量在线状态指示器缓存
        cacheProvider.setOnline(uidLong, client.getSessionId().toString());
        
        // 为 Session 缓存设置过期时间为 24 小时，防止强杀服务遗留垃圾 Key 永久残留
        try {
            wsRedissonClient.getMap(client.getSessionId().toString()).expire(24, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("【WebSocket】为 SessionMap 设置过期时间失败", e);
        }
        
        client.joinRoom(Connector.ROOM + uid);
        log.info("【WebSocket】客户端连接成功，UID: {}, SessionId: {}, 已加入专属房间 user_room:{}", uidLong, client.getSessionId(), uid);
    }
}

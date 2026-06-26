package com.summit.stp.common.infrastructure.websocket.listener;

import com.corundumstudio.socketio.SocketIOClient;
import com.summit.stp.common.infrastructure.websocket.Connector;
import com.summit.stp.common.infrastructure.websocket.provide.CacheProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DisconnectListenerTest {

    @Test
    void testOnDisconnectClearsRedisCache() {
        // Arrange
        CacheProvider cacheProvider = mock(CacheProvider.class);
        RedissonClient wsRedissonClient = mock(RedissonClient.class);
        SocketIOClient client = mock(SocketIOClient.class);
        RMap<Object, Object> rMap = mock(RMap.class);

        UUID sessionId = UUID.randomUUID();
        when(client.getSessionId()).thenReturn(sessionId);
        when(client.get(Connector.UID)).thenReturn("123");
        when(wsRedissonClient.getMap(sessionId.toString())).thenReturn(rMap);

        DisconnectListener disconnectListener = new DisconnectListener(cacheProvider, wsRedissonClient);

        // Act
        disconnectListener.onDisconnect(client);

        // Assert
        verify(cacheProvider).removeOnline(123L, sessionId.toString());
        verify(wsRedissonClient).getMap(sessionId.toString());
        verify(rMap).delete();
        verify(client).disconnect();
    }
}

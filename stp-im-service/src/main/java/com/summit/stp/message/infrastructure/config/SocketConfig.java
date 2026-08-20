package com.summit.stp.message.infrastructure.config;

import com.corundumstudio.socketio.AckMode;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.store.RedissonStoreFactory;
import com.summit.stp.message.infrastructure.properties.ImProperties;
import com.summit.stp.message.infrastructure.websocket.Author;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SocketConfig {
    private final Author author;
    private final RedissonClient wsRedissonClient;
    private final ImProperties imProperties;


    public SocketConfig(Author author, @Qualifier("wsRedissonClient") RedissonClient wsRedissonClient, ImProperties imProperties) {
        this.author = author;
        this.wsRedissonClient = wsRedissonClient;
        this.imProperties = imProperties;
    }

    @Bean(destroyMethod = "stop")
    public SocketIOServer socketIOServer(){
        // 清理由于上次服务器异常关闭/强杀遗留在 Redis 中的所有旧连接 Key
        try {
            wsRedissonClient.getKeys().deleteByPattern("*");
            log.info("【WebSocket】已成功清理 Redis 中遗留的旧 WebSocket 连接缓存");
        } catch (Exception e) {
            log.warn("【WebSocket】清理遗留连接缓存时发生异常", e);
        }

        com.corundumstudio.socketio.Configuration configuration = getConfiguration();

        SocketIOServer socketIOServer = new SocketIOServer(configuration);

        socketIOServer.start();
        log.info("【ws】连接通道已开启 (已启用专属隔离的分布式 Redis 托管)");
        return socketIOServer;
    }

    private com.corundumstudio.socketio.@NonNull Configuration getConfiguration() {
        com.corundumstudio.socketio.Configuration configuration = new com.corundumstudio.socketio.Configuration();
        configuration.setPingInterval(imProperties.getPingInterval());
        configuration.setPort(imProperties.getPort());
        configuration.setHostname(imProperties.getHostname());
        configuration.setNeedClientAuth(true);
        configuration.setEnableCors(true);
        configuration.setAckMode(AckMode.MANUAL);
        configuration.setAuthorizationListener(author);
        configuration.setPingTimeout(imProperties.getPingTimeout());

        // 传入由 RedissonConfig 统一配置的专属 wsRedissonClient 存储工厂
        configuration.setStoreFactory(new RedissonStoreFactory(wsRedissonClient));
        return configuration;
    }
}

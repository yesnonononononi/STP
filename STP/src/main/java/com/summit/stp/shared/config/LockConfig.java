package com.summit.stp.shared.config;

import lombok.Builder;
import org.redisson.Redisson;
import org.redisson.RedissonLock;

import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LockConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:1}")
    private int redisDatabase;

    @Bean
    public RedissonClient  redissonClient(){
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setDatabase(redisDatabase)
                .setConnectTimeout(3000)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(10)
                .setIdleConnectionTimeout(10000)
                .setRetryAttempts(3);

        if(redisPassword != null && !redisPassword.isEmpty()){
            config.useSentinelServers().setPassword(redisPassword);
        }
        return  Redisson.create(config);
    }
}

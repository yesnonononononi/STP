package com.summit.stp.shared.config;

import com.summit.stp.message.infrastructure.constants.ImConstants;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.NameMapper;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:1}")
    private int redisDatabase;

    private Config createBaseConfig() {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setDatabase(redisDatabase)
                .setConnectTimeout(3000)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(10)
                .setIdleConnectionTimeout(10000)
                .setRetryAttempts(3);

        if (redisPassword != null && !redisPassword.isEmpty()) {
            singleServerConfig.setPassword(redisPassword);
        }
        return config;
    }

    @Primary
    @Bean("redissonClient")
    public RedissonClient redissonClient() {
        return Redisson.create(createBaseConfig());
    }

    @Bean(name = "wsRedissonClient", destroyMethod = "shutdown")
    public RedissonClient wsRedissonClient() {
        Config config = createBaseConfig();
        config.setNameMapper(new NameMapper() {
            private final String prefix = ImConstants.Cache.CONNECTION_KEY;

            @Override
            public String map(String name) {
                if (name.startsWith(prefix)) {
                    return name;
                }
                return prefix + name;
            }

            @Override
            public String unmap(String name) {
                if (name.startsWith(prefix)) {
                    return name.substring(prefix.length());
                }
                return name;
            }
        });
        return Redisson.create(config);
    }
}

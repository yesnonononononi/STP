package com.summit.stp.comment.it;

import com.summit.stp.common.config.RedisConfig;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 评论模块并发集成测试基类（真实 Redis）。
 *
 * <p>复用生产环境 {@link RedisConfig} 构建 {@link RedisTemplate}，保证 key / value
 * 序列化行为与线上一致；仅拉起 Redis 容器，避免 Nacos / MinIO / RabbitMQ 等无关依赖，
 * 降低 flaky 风险。</p>
 *
 * <p>测试类继承本类并标注 {@code @Tag("integration")}。</p>
 */
public abstract class AbstractCommentRedisIntegrationTest {

    protected static final int REDIS_PORT = 6379;

    protected static final GenericContainer<?> REDIS;

    protected static final RedisTemplate<String, Object> REDIS_TEMPLATE;

    static {
        REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(REDIS_PORT);
        REDIS.start();

        LettuceConnectionFactory connectionFactory =
                new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(REDIS_PORT));
        connectionFactory.afterPropertiesSet();
        REDIS_TEMPLATE = new RedisConfig().redisTemplate(connectionFactory);
    }
}

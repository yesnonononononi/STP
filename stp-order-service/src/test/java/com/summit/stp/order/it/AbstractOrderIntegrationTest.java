package com.summit.stp.order.it;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * 订单服务集成测试基类。
 *
 * <p>基于 Testcontainers 启动真实的 MySQL / RabbitMQ / Redis 容器，三者随测试类加载时
 * 启动、JVM 退出时由 Testcontainers 回收（静态复用，多个 IT 共享）。连接信息通过
 * {@link DynamicPropertySource} 注入，绝不使用固定 localhost。</p>
 *
 * <p>使用方式：具体 IT 继承本类并标注 {@code @Tag("integration")}，默认构建会跳过，
 * 通过 {@code -Pintegration} 激活。</p>
 */
@SpringBootTest
@ActiveProfiles("it")
public abstract class AbstractOrderIntegrationTest {

    /** MySQL 镜像（与本机/CI 预置镜像保持一致，避免临时拉取）。 */
    private static final String MYSQL_IMAGE = "mysql:8.0.30";

    /** RabbitMQ 镜像。 */
    private static final String RABBITMQ_IMAGE = "rabbitmq:4.3.4";

    /** Redis 镜像（Testcontainers 2.x 未提供官方 Redis 模块，使用通用容器）。 */
    private static final String REDIS_IMAGE = "redis:7-alpine";

    /** Redis 容器内暴露端口。 */
    private static final int REDIS_PORT = 6379;

    protected static final MySQLContainer MYSQL;

    protected static final RabbitMQContainer RABBITMQ;

    protected static final GenericContainer<?> REDIS;

    static {
        MYSQL = new MySQLContainer(DockerImageName.parse(MYSQL_IMAGE))
                .withDatabaseName("stp")
                .withUsername("stp")
                .withPassword("stp")
                .withInitScript("sql/order-init.sql");
        RABBITMQ = new RabbitMQContainer(DockerImageName.parse(RABBITMQ_IMAGE));
        REDIS = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                .withExposedPorts(REDIS_PORT);

        MYSQL.start();
        RABBITMQ.start();
        REDIS.start();
    }

    /**
     * 将容器真实连接信息注入 Spring 上下文。
     *
     * @param registry 动态属性注册表
     */
    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", AbstractOrderIntegrationTest::mysqlJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(REDIS_PORT));

        registry.add("spring.rabbitmq.host", RABBITMQ::getHost);
        registry.add("spring.rabbitmq.port", RABBITMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBITMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBITMQ::getAdminPassword);
    }

    /**
     * 构造带必要连接参数的 MySQL JDBC URL。
     *
     * <p>补充 {@code allowPublicKeyRetrieval} 以兼容 caching_sha2_password 认证，
     * 并补齐时区与字符集参数，行为与生产 Nacos 下发的 jdbc 配置一致。</p>
     *
     * @return 可直接使用的 JDBC URL
     */
    private static String mysqlJdbcUrl() {
        String url = MYSQL.getJdbcUrl();
        String params = "useUnicode=true&characterEncoding=utf-8&useSSL=false"
                + "&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&tinyInt1isBit=false";
        return url + (url.contains("?") ? "&" : "?") + params;
    }
}

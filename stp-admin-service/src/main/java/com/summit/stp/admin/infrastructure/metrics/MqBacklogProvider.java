package com.summit.stp.admin.infrastructure.metrics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * RabbitMQ 待消费消息积压量读取器。
 *
 * <p>数据来源：RabbitMQ 管理 HTTP API {@code GET {management-url}/api/queues}（Basic Auth），
 * 汇总所有队列的 {@code messages_ready} 字段。</p>
 *
 * <p>配置项（见 {@code application.yaml}）：
 * {@code stp.metrics.rabbitmq.management-url / username / password}，
 * 均支持通过环境变量 {@code RABBITMQ_MANAGEMENT_URL / RABBITMQ_DEFAULT_USER / RABBITMQ_DEFAULT_PASS} 覆盖。</p>
 *
 * <p>未配置或取数失败时返回 {@code null}（表示「未知」），绝不返回 0。</p>
 */
@Slf4j
@Component
public class MqBacklogProvider {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(3);

    private final JsonMapper objectMapper;
    private final String managementUrl;
    private final String username;
    private final String password;
    private final HttpClient httpClient;

    public MqBacklogProvider(JsonMapper objectMapper,
                             @Value("${stp.metrics.rabbitmq.management-url:}") String managementUrl,
                             @Value("${stp.metrics.rabbitmq.username:}") String username,
                             @Value("${stp.metrics.rabbitmq.password:}") String password) {
        this.objectMapper = objectMapper;
        this.managementUrl = managementUrl;
        this.username = username;
        this.password = password;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    /**
     * 汇总各队列 {@code messages_ready} 得到总积压量。
     *
     * @return 总积压条数；未配置管理 API 或取数失败时返回 null（未知）
     */
    public Long getTotalBacklog() {
        if (managementUrl == null || managementUrl.isBlank()) {
            log.debug("【后台大盘】[指标] 未配置 RabbitMQ 管理 API(management-url), 跳过 MQ 积压采集");
            return null;
        }
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(buildQueuesUrl(managementUrl)))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Accept", "application/json")
                    .GET();
            if (username != null && !username.isBlank()) {
                builder.header("Authorization", "Basic " + basicAuth(username, password));
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("【后台大盘】[指标] RabbitMQ 管理 API 返回非 200: status={}", response.statusCode());
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            if (root == null || !root.isArray()) {
                log.warn("【后台大盘】[指标] RabbitMQ 管理 API 响应格式非数组, 返回未知(null)");
                return null;
            }
            long total = 0L;
            for (JsonNode queue : root) {
                total += queue.path("messages_ready").asLong(0L);
            }
            return total;
        } catch (Exception e) {
            log.warn("【后台大盘】[指标] 采集 RabbitMQ 积压量异常, 返回未知(null)", e);
            return null;
        }
    }

    private String buildQueuesUrl(String baseUrl) {
        String trimmed = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return trimmed + "/api/queues";
    }

    private String basicAuth(String user, String pass) {
        String credentials = user + ":" + (pass == null ? "" : pass);
        return Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}

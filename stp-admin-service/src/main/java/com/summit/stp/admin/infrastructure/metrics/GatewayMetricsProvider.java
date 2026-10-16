package com.summit.stp.admin.infrastructure.metrics;

import com.summit.stp.common.constants.MetricsConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 网关运行指标读取器。
 *
 * <p>数据来源：{@code stp-gateway} 的 {@code GatewayMetricsFilter} 按分钟写入 Redis 的计数，
 * Key 契约见 {@link MetricsConstants}（前缀 + {@code yyyyMMddHHmm}，TTL 180s）。</p>
 *
 * <p>取数失败/指标缺失时统一返回 {@code null}（表示「未知」），绝不返回 0，
 * 以避免把「取不到数」伪装成「真实的 0」。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayMetricsProvider {

    private static final DateTimeFormatter MINUTE_FORMATTER =
            DateTimeFormatter.ofPattern(MetricsConstants.MINUTE_PATTERN);

    private static final double SECONDS_PER_MINUTE = 60.0;

    private final StringRedisTemplate redisTemplate;

    /**
     * 计算网关实时 QPS。
     *
     * <p>口径：优先用「当前分钟 + 上一分钟」的请求数做平滑，即 {@code (reqCur + reqPrev) / 120.0}；
     * 上一分钟无数据时退化为 {@code reqCur / 60.0}。当前分钟无指标则返回 null。</p>
     *
     * @return QPS（保留 2 位小数），或 null 表示未知
     */
    public Double getGatewayQps() {
        if (redisTemplate == null) {
            return null;
        }
        try {
            LocalDateTime now = LocalDateTime.now();
            String currentMinute = now.format(MINUTE_FORMATTER);
            String previousMinute = now.minusMinutes(1).format(MINUTE_FORMATTER);

            Long currentReq = readCounter(MetricsConstants.GATEWAY_REQ_KEY_PREFIX + currentMinute);
            if (currentReq == null) {
                // 当前分钟没有写入指标 → 视为未知
                return null;
            }
            Long previousReq = readCounter(MetricsConstants.GATEWAY_REQ_KEY_PREFIX + previousMinute);

            double qps = (previousReq != null)
                    ? (currentReq + previousReq) / (SECONDS_PER_MINUTE * 2)
                    : currentReq / SECONDS_PER_MINUTE;
            return round2(qps);
        } catch (Exception e) {
            log.warn("【后台大盘】[指标] 读取网关 QPS 异常, 返回未知(null)", e);
            return null;
        }
    }

    /**
     * 计算网关平均响应耗时（毫秒）。
     *
     * <p>口径：{@code avgResponseTimeMs = rt_sum / rt_cnt}；当该分钟无请求（cnt 缺失或为 0）时返回 null。</p>
     *
     * @return 平均耗时毫秒（保留 2 位小数），或 null 表示未知
     */
    public Double getAvgResponseTimeMs() {
        if (redisTemplate == null) {
            return null;
        }
        try {
            String currentMinute = LocalDateTime.now().format(MINUTE_FORMATTER);
            Long rtSum = readCounter(MetricsConstants.GATEWAY_RT_SUM_KEY_PREFIX + currentMinute);
            Long rtCnt = readCounter(MetricsConstants.GATEWAY_RT_CNT_KEY_PREFIX + currentMinute);
            if (rtSum == null || rtCnt == null || rtCnt <= 0) {
                return null;
            }
            return round2((double) rtSum / rtCnt);
        } catch (Exception e) {
            log.warn("【后台大盘】[指标] 读取网关平均耗时异常, 返回未知(null)", e);
            return null;
        }
    }

    private Long readCounter(String key) {
        String raw = redisTemplate.opsForValue().get(key);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            log.debug("【后台大盘】[指标] 指标值非法, key={}, value={}", key, raw);
            return null;
        }
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

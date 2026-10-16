package com.summit.stp.gateway.filter;

import com.summit.stp.common.constants.MetricsConstants;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 网关指标采集过滤器（大盘指标「写入端」）。
 *
 * <p>作为最外层过滤器（{@link Ordered#HIGHEST_PRECEDENCE}）先进入、最后完成，
 * 从而统计「完整」的网关请求耗时——包含鉴权过滤、路由与下游响应全过程；
 * 同时也能覆盖鉴权短路（401/403）的请求，避免大盘 QPS 漏统计。</p>
 *
 * <p>契约（见 {@link MetricsConstants}）：按分钟分桶写入
 * {@code stp:metrics:gateway:req:<yyyyMMddHHmm>}、
 * {@code stp:metrics:gateway:rt_sum:<yyyyMMddHHmm>}、
 * {@code stp:metrics:gateway:rt_cnt:<yyyyMMddHHmm>}，并统一 expire 180s。</p>
 *
 * <p>采集失败必须静默：任何异常都被吞掉，绝不影响主请求流程。</p>
 */
@Slf4j
@Component
public class GatewayMetricsFilter implements GlobalFilter, Ordered {

    private static final DateTimeFormatter MINUTE_FORMATTER =
            DateTimeFormatter.ofPattern(MetricsConstants.MINUTE_PATTERN);

    private static final Duration METRICS_TTL =
            Duration.ofSeconds(MetricsConstants.GATEWAY_METRICS_TTL_SECONDS);

    /**
     * WebFlux 场景使用响应式 Redis 模板，避免阻塞事件循环线程。
     * required = false：即便环境未装配响应式模板，也保证应用可正常启动（此时静默跳过采集）。
     */
    @Autowired(required = false)
    private ReactiveStringRedisTemplate redisTemplate;

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        long startNanos = System.nanoTime();
        return chain.filter(exchange).doFinally(signalType -> record(startNanos));
    }

    /**
     * 采集当前分钟的请求量 / 耗时累加 / 耗时样本数，并刷新 TTL。
     *
     * <p>注意：Redis 的 INCR / INCRBY 不会自动附带 TTL，因此必须对每个 key
     * 「先 increment、后 expire」**串行**执行（{@code increment(...).then(expire(...))}）。
     * 若像早前那样把 increment 与 expire 作为两个独立订阅并发发出，首个请求可能出现
     * expire 先于 increment 落地（此时 key 尚不存在，expire 空操作），随后 increment
     * 建出的 key 将永久无 TTL 而残留。</p>
     *
     * <p>全部操作非阻塞（fire-and-forget），任何异常均静默忽略，绝不影响主流程。</p>
     */
    private void record(long startNanos) {
        if (redisTemplate == null) {
            return;
        }
        try {
            long costMs = (System.nanoTime() - startNanos) / 1_000_000L;
            String minute = LocalDateTime.now().format(MINUTE_FORMATTER);

            String reqKey = MetricsConstants.GATEWAY_REQ_KEY_PREFIX + minute;
            String rtSumKey = MetricsConstants.GATEWAY_RT_SUM_KEY_PREFIX + minute;
            String rtCntKey = MetricsConstants.GATEWAY_RT_CNT_KEY_PREFIX + minute;

            ReactiveValueOperations<String, String> ops = redisTemplate.opsForValue();

            Mono.when(
                    ops.increment(reqKey).then(redisTemplate.expire(reqKey, METRICS_TTL)),
                    ops.increment(rtSumKey, costMs).then(redisTemplate.expire(rtSumKey, METRICS_TTL)),
                    ops.increment(rtCntKey).then(redisTemplate.expire(rtCntKey, METRICS_TTL))
            ).subscribe(ignored -> { }, e -> log.debug("【网关指标】采集失败（已忽略）", e));
        } catch (Exception e) {
            // 指标采集为旁路能力，失败静默处理，绝不干扰主流程
            log.debug("【网关指标】采集失败（已忽略）", e);
        }
    }

    @Override
    public int getOrder() {
        // 最小 order：作为最外层过滤器，先进入、最后完成，统计完整网关耗时
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

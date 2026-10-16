package com.summit.stp.common.constants;

/**
 * 系统运行指标 Redis Key 契约常量（共享契约，禁止各模块重复定义）。
 * <p>
 * 写入端：{@code stp-gateway} 的 {@code GatewayMetricsFilter} —— 按分钟聚合网关请求数与响应耗时。
 * 读取端：{@code stp-admin-service} 的 {@code GatewayMetricsProvider} —— 供后台大盘 system-status 展示。
 * <p>
 * Key 规则：{@code <前缀> + yyyyMMddHHmm}，TTL 为 {@link #GATEWAY_METRICS_TTL_SECONDS} 秒。
 */
public final class MetricsConstants {

    private MetricsConstants() {
    }

    /** 网关请求数计数（按分钟）。完整 key = 前缀 + 分钟串。 */
    public static final String GATEWAY_REQ_KEY_PREFIX = "stp:metrics:gateway:req:";

    /** 网关响应耗时累加值（毫秒，按分钟）。完整 key = 前缀 + 分钟串。 */
    public static final String GATEWAY_RT_SUM_KEY_PREFIX = "stp:metrics:gateway:rt_sum:";

    /** 网关响应耗时样本数（按分钟）。完整 key = 前缀 + 分钟串。 */
    public static final String GATEWAY_RT_CNT_KEY_PREFIX = "stp:metrics:gateway:rt_cnt:";

    /** 分钟级指标 key 的存活时间（秒）。 */
    public static final long GATEWAY_METRICS_TTL_SECONDS = 180L;

    /** 分钟级 key 使用的时间格式。 */
    public static final String MINUTE_PATTERN = "yyyyMMddHHmm";
}

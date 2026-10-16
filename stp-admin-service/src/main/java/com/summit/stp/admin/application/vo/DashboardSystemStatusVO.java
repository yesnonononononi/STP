package com.summit.stp.admin.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSystemStatusVO implements DashboardDegradable {

    /**
     * 网关实时 QPS（来自网关写入 Redis 的分钟级计数）。
     * null 表示「未知/取数失败」，前端应展示为 "—"，不得当作 0。
     */
    private Double gatewayQps;

    /**
     * 网关平均响应耗时（毫秒）= rt_sum / rt_cnt。
     * null 表示「未知/取数失败」（如该分钟内无请求或指标不可用），前端应展示为 "—"。
     */
    private Double avgResponseTimeMs;

    private Double cpuUsage;
    private Double memoryUsage;

    /**
     * 应用级缓存命中率（%）。
     * 口径：仅统计后台大盘 DashboardCacheProvider 自身的缓存读命中/未命中，
     * 不使用 Redis 服务器全局 keyspace_hits/misses（后者包含全站所有业务 Key，不能代表本应用缓存效果）。
     */
    private Double redisHitRate;

    /**
     * RabbitMQ 待消费消息积压总量（各队列 messages_ready 之和）。
     * null 表示「未知/取数失败」（如管理 API 未配置或不可达），前端应展示为 "—"。
     */
    private Long mqBacklogCount;

    private List<ServiceHealthItem> services;

    /**
     * 降级标记：true 表示网关指标 / MQ 积压等实时指标取数失败，字段为 null 而非真实 0
     */
    private Boolean degraded;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceHealthItem {
        private String name;
        private String status;
        private Integer instances;
        private String uptime;
    }
}

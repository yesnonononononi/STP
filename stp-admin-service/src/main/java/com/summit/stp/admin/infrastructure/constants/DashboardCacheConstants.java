package com.summit.stp.admin.infrastructure.constants;

/**
 * 后台大盘 Redis 缓存常量类
 * 统一管理缓存 Key、TTL 以及应用级缓存命中率统计 Key
 */
public class DashboardCacheConstants {

    /**
     * 大盘全局缓存过期时间：3 分钟（180 秒）
     */
    public static final long CACHE_TTL_SECONDS = 180L;

    /**
     * 空结果（null / 空列表）短 TTL：防止缓存穿透，同时避免长时间固化空数据
     */
    public static final long EMPTY_CACHE_TTL_SECONDS = 30L;

    /**
     * 降级态（远程依赖取数失败）短 TTL：避免把「失败态」当作「真实 0」缓存 3 分钟
     */
    public static final long DEGRADED_CACHE_TTL_SECONDS = 15L;

    /**
     * 分布式锁租约时间（秒）：持有锁的最长时间，防止持锁线程异常导致死锁
     */
    public static final long LOCK_LEASE_SECONDS = 10L;

    /**
     * 防击穿分布式锁 Key 前缀
     */
    public static final String CACHE_LOCK_KEY_PREFIX = "admin:dashboard:lock:";

    public static final String DASHBOARD_OVERVIEW_KEY = "admin:dashboard:overview";
    public static final String DASHBOARD_TRENDS_KEY_PREFIX = "admin:dashboard:trends:";
    public static final String DASHBOARD_MEMBER_STATS_KEY = "admin:dashboard:member-stats";
    public static final String DASHBOARD_CONTENT_STATS_KEY_PREFIX = "admin:dashboard:content-stats:";
    public static final String DASHBOARD_SYSTEM_STATUS_KEY = "admin:dashboard:system-status";
    public static final String DASHBOARD_RECENT_ACTIVITIES_KEY_PREFIX = "admin:dashboard:recent-activities:";

    /**
     * 应用级缓存命中计数 Key（大盘自身的缓存读命中次数）
     * 口径说明：仅统计 DashboardCacheProvider 的读操作，不使用 Redis 服务器全局 keyspace_hits/misses
     */
    public static final String CACHE_HIT_KEY = "stp:metrics:cache:hit";

    /**
     * 应用级缓存未命中计数 Key（大盘自身的缓存读未命中次数）
     */
    public static final String CACHE_MISS_KEY = "stp:metrics:cache:miss";
}

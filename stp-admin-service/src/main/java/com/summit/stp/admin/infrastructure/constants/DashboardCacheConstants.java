package com.summit.stp.admin.infrastructure.constants;

/**
 * 后台大盘 Redis 缓存常量类
 * 统一管理缓存 Key 与 5 分钟 (300 秒) 过期时间
 */
public class DashboardCacheConstants {

    /**
     * 大盘全局缓存过期时间：3 分钟（180 秒）
     */
    public static final long CACHE_TTL_SECONDS = 180L;

    public static final String DASHBOARD_OVERVIEW_KEY = "admin:dashboard:overview";
    public static final String DASHBOARD_TRENDS_KEY_PREFIX = "admin:dashboard:trends:";
    public static final String DASHBOARD_MEMBER_STATS_KEY = "admin:dashboard:member-stats";
    public static final String DASHBOARD_CONTENT_STATS_KEY_PREFIX = "admin:dashboard:content-stats:";
    public static final String DASHBOARD_SYSTEM_STATUS_KEY = "admin:dashboard:system-status";
    public static final String DASHBOARD_RECENT_ACTIVITIES_KEY_PREFIX = "admin:dashboard:recent-activities:";
}

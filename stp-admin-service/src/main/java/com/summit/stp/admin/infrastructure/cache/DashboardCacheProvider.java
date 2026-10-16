package com.summit.stp.admin.infrastructure.cache;

import java.util.List;
import java.util.function.Supplier;

/**
 * 后台大盘缓存操作 Provider 接口
 */
public interface DashboardCacheProvider {

    /**
     * 读取缓存，未命中则由 supplier 回源并写回缓存（Redisson 分布式锁防击穿）。
     *
     * <p>约定：supplier 返回 null 表示回源失败/无数据，会以「空值哨兵 + 短 TTL」缓存以防穿透；
     * 若返回对象实现 {@link com.summit.stp.admin.application.vo.DashboardDegradable}
     * 且为降级态，则以短 TTL 缓存，避免把失败态固化为真实数据。</p>
     */
    <T> T getOrSet(String cacheKey, Class<T> clazz, Supplier<T> supplier);

    /**
     * 列表版缓存读写，语义同 {@link #getOrSet}，空列表以短 TTL 缓存防穿透。
     */
    <T> List<T> getOrSetList(String cacheKey, Class<T> elementClazz, Supplier<List<T>> supplier);

    /**
     * 计算应用级缓存命中率（%）。
     *
     * <p>口径：基于本应用自增的 {@code stp:metrics:cache:hit} / {@code stp:metrics:cache:miss}
     * 计数，仅反映后台大盘自身缓存效果；不使用 Redis 服务器全局 keyspace_hits/misses。</p>
     *
     * @return 命中率百分比（保留 1 位小数）；无任何统计数据时返回 0.0
     */
    double getAppCacheHitRate();
}

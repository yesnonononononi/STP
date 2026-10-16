package com.summit.stp.admin.infrastructure.cache;

import com.summit.stp.admin.application.vo.DashboardDegradable;
import com.summit.stp.admin.infrastructure.constants.DashboardCacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 后台大盘缓存操作 Provider 实现类。
 *
 * <p>防击穿：使用 <b>Redisson 分布式锁</b>（{@code tryLock(0, 10, SECONDS)}），
 * 替换原先仅在本 JVM 生效的 {@code ConcurrentHashMap} 本地锁，保证多实例部署下只有一个实例回源。</p>
 *
 * <p>防穿透：null / 空列表以「空值哨兵 + 短 TTL」写回缓存，避免对空数据的重复回源。</p>
 *
 * <p>防污染：降级态（{@link DashboardDegradable#getDegraded()} 为 true）结果仅以短 TTL 缓存，
 * 不会把「取数失败」固化成「真实 0」。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardCacheProviderImpl implements DashboardCacheProvider {

    /**
     * 空值哨兵：写入 Redis 用于标记「已确认回源为空」，读到时视为命中并返回 null
     */
    private static final String NULL_SENTINEL = "__STP_DASHBOARD_NULL__";

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper objectMapper;
    private final RedissonClient redissonClient;

    @Override
    public <T> T getOrSet(String cacheKey, Class<T> clazz, Supplier<T> supplier) {
        return getOrLoad(
                cacheKey,
                () -> readObjectFromCache(cacheKey, clazz),
                supplier,
                value -> writeObjectToCache(cacheKey, value)
        );
    }

    @Override
    public <T> List<T> getOrSetList(String cacheKey, Class<T> elementClazz, Supplier<List<T>> supplier) {
        return getOrLoad(
                cacheKey,
                () -> readListFromCache(cacheKey, elementClazz),
                supplier,
                value -> writeListToCache(cacheKey, value)
        );
    }

    @Override
    public double getAppCacheHitRate() {
        if (redisTemplate == null) {
            return 0.0;
        }
        try {
            long hits = parseCounter(redisTemplate.opsForValue().get(DashboardCacheConstants.CACHE_HIT_KEY));
            long misses = parseCounter(redisTemplate.opsForValue().get(DashboardCacheConstants.CACHE_MISS_KEY));
            long total = hits + misses;
            if (total <= 0) {
                return 0.0;
            }
            return Math.round(hits * 100.0 / total * 10.0) / 10.0;
        } catch (Exception e) {
            log.warn("【后台大盘】[缓存] 计算应用级缓存命中率异常", e);
            return 0.0;
        }
    }

    // ===================== 通用缓存读写模板 (#30 抽公共泛型方法) =====================

    /**
     * 通用「读缓存 → 未命中加分布式锁 → 二次读 → 回源 → 写缓存」模板。
     *
     * @param reader 缓存读取器，返回带「是否命中」标记的结果
     * @param supplier 回源逻辑
     * @param writer 缓存写入逻辑
     */
    private <T> T getOrLoad(String cacheKey,
                            Supplier<Cached<T>> reader,
                            Supplier<T> supplier,
                            Consumer<T> writer) {
        Cached<T> first = reader.get();
        if (first.hit()) {
            recordHit(cacheKey);
            return first.value();
        }
        recordMiss(cacheKey);

        RLock lock = null;
        boolean locked = false;
        try {
            if (redissonClient == null) {
                // 无分布式锁组件时降级为本地单实例直通（保持功能可用）
                locked = true;
            } else {
                lock = redissonClient.getLock(DashboardCacheConstants.CACHE_LOCK_KEY_PREFIX + cacheKey);
                // waitTime=0：不阻塞等待，拿不到锁立即回源，避免请求线程堆积
                locked = lock.tryLock(0, DashboardCacheConstants.LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("【后台大盘】[缓存] 获取分布式锁被中断, 降级无锁回源: {}", cacheKey, e);
            return supplier.get();
        } catch (Exception e) {
            log.warn("【后台大盘】[缓存] 获取分布式锁异常, 降级无锁回源: {}", cacheKey, e);
            return supplier.get();
        }

        if (!locked) {
            log.info("【后台大盘】[缓存] 未获取到分布式锁, 跳过加锁直接取数(不写缓存): {}", cacheKey);
            return supplier.get();
        }

        try {
            // 双重校验：拿到锁后再次确认缓存
            Cached<T> second = reader.get();
            if (second.hit()) {
                return second.value();
            }
            T data = supplier.get();
            writer.accept(data);
            return data;
        } finally {
            if (lock != null && lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.warn("【后台大盘】[缓存] 释放分布式锁异常: {}", cacheKey, e);
                }
            }
        }
    }

    private <T> Cached<T> readObjectFromCache(String cacheKey, Class<T> clazz) {
        if (redisTemplate == null) {
            return Cached.miss();
        }
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (json == null || json.isBlank()) {
                return Cached.miss();
            }
            if (NULL_SENTINEL.equals(json)) {
                return Cached.hit(null);
            }
            return Cached.hit(objectMapper.readValue(json, clazz));
        } catch (Exception e) {
            log.warn("【后台大盘】[缓存读取异常] Key: {}", cacheKey, e);
            return Cached.miss();
        }
    }

    private <T> Cached<List<T>> readListFromCache(String cacheKey, Class<T> elementClazz) {
        if (redisTemplate == null) {
            return Cached.miss();
        }
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (json == null || json.isBlank()) {
                return Cached.miss();
            }
            List<T> list = objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementClazz)
            );
            return Cached.hit(list);
        } catch (Exception e) {
            log.warn("【后台大盘】[缓存读取异常] Key: {}", cacheKey, e);
            return Cached.miss();
        }
    }

    private <T> void writeObjectToCache(String cacheKey, T data) {
        if (redisTemplate == null) {
            return;
        }
        try {
            long ttl;
            String json;
            if (data == null) {
                // 空值哨兵：防穿透
                json = NULL_SENTINEL;
                ttl = DashboardCacheConstants.EMPTY_CACHE_TTL_SECONDS;
            } else {
                json = objectMapper.writeValueAsString(data);
                ttl = isDegraded(data)
                        ? DashboardCacheConstants.DEGRADED_CACHE_TTL_SECONDS
                        : DashboardCacheConstants.CACHE_TTL_SECONDS;
            }
            redisTemplate.opsForValue().set(cacheKey, json, ttl, TimeUnit.SECONDS);
            log.info("【后台大盘】[缓存写入] Key: {}, TTL: {}s, 降级: {}", cacheKey, ttl, data != null && isDegraded(data));
        } catch (Exception e) {
            log.warn("【后台大盘】[缓存写入异常] Key: {}", cacheKey, e);
        }
    }

    private <T> void writeListToCache(String cacheKey, List<T> list) {
        if (redisTemplate == null) {
            return;
        }
        try {
            boolean empty = (list == null || list.isEmpty());
            long ttl = empty ? DashboardCacheConstants.EMPTY_CACHE_TTL_SECONDS : DashboardCacheConstants.CACHE_TTL_SECONDS;
            String json = objectMapper.writeValueAsString(list == null ? List.of() : list);
            redisTemplate.opsForValue().set(cacheKey, json, ttl, TimeUnit.SECONDS);
            log.info("【后台大盘】[缓存写入] Key: {}, TTL: {}s, 空列表: {}", cacheKey, ttl, empty);
        } catch (Exception e) {
            log.warn("【后台大盘】[缓存写入异常] Key: {}", cacheKey, e);
        }
    }

    private boolean isDegraded(Object data) {
        return data instanceof DashboardDegradable degradable && Boolean.TRUE.equals(degradable.getDegraded());
    }

    private void recordHit(String cacheKey) {
        log.info("【后台大盘】[缓存命中] Key: {}", cacheKey);
        incrementCounter(DashboardCacheConstants.CACHE_HIT_KEY);
    }

    private void recordMiss(String cacheKey) {
        log.info("【后台大盘】[缓存未命中] Key: {}", cacheKey);
        incrementCounter(DashboardCacheConstants.CACHE_MISS_KEY);
    }

    private void incrementCounter(String key) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.debug("【后台大盘】[缓存] 自增命中计数失败: {}", key, e);
        }
    }

    private long parseCounter(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * 缓存读取结果：区分「命中（值可能为 null）」与「未命中」
     */
    private record Cached<T>(boolean hit, T value) {
        static <T> Cached<T> hit(T value) {
            return new Cached<>(true, value);
        }

        static <T> Cached<T> miss() {
            return new Cached<>(false, null);
        }
    }
}

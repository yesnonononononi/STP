package com.summit.stp.admin.infrastructure.cache;

import com.summit.stp.admin.infrastructure.constants.DashboardCacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 后台大盘缓存操作 Provider 实现类
 * 内置防击穿互斥锁 (Double-Checked Lock) 与防缓存污染机制
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardCacheProviderImpl implements DashboardCacheProvider {

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper objectMapper;
    private final Map<String, Object> lockMap = new ConcurrentHashMap<>();

    @Override
    public <T> T getOrSet(String cacheKey, Class<T> clazz, Supplier<T> supplier) {
        T cachedData = readFromCache(cacheKey, clazz);
        if (cachedData != null) {
            return cachedData;
        }

        // 双重校验互斥锁，防止并发击穿
        Object lock = lockMap.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            try {
                // 二次判断缓存
                cachedData = readFromCache(cacheKey, clazz);
                if (cachedData != null) {
                    return cachedData;
                }

                T data = supplier.get();
                if (data != null && redisTemplate != null) {
                    try {
                        String json = objectMapper.writeValueAsString(data);
                        redisTemplate.opsForValue().set(cacheKey, json, DashboardCacheConstants.CACHE_TTL_SECONDS, TimeUnit.SECONDS);
                        log.info("【后台大盘】[缓存写入] Key: {}, TTL: {}s", cacheKey, DashboardCacheConstants.CACHE_TTL_SECONDS);
                    } catch (Exception e) {
                        log.warn("【后台大盘】[缓存写入异常] Key: {}", cacheKey, e);
                    }
                }
                return data;
            } finally {
                lockMap.remove(cacheKey);
            }
        }
    }

    @Override
    public <T> List<T> getOrSetList(String cacheKey, Class<T> elementClazz, Supplier<List<T>> supplier) {
        List<T> cachedList = readListFromCache(cacheKey, elementClazz);
        if (cachedList != null) {
            return cachedList;
        }

        Object lock = lockMap.computeIfAbsent(cacheKey, k -> new Object());
        synchronized (lock) {
            try {
                cachedList = readListFromCache(cacheKey, elementClazz);
                if (cachedList != null) {
                    return cachedList;
                }

                List<T> list = supplier.get();
                if (list != null && !list.isEmpty() && redisTemplate != null) {
                    try {
                        String json = objectMapper.writeValueAsString(list);
                        redisTemplate.opsForValue().set(cacheKey, json, DashboardCacheConstants.CACHE_TTL_SECONDS, TimeUnit.SECONDS);
                        log.info("【后台大盘】[缓存写入] Key: {}, TTL: {}s", cacheKey, DashboardCacheConstants.CACHE_TTL_SECONDS);
                    } catch (Exception e) {
                        log.warn("【后台大盘】[缓存写入异常] Key: {}", cacheKey, e);
                    }
                }
                return list;
            } finally {
                lockMap.remove(cacheKey);
            }
        }
    }

    private <T> T readFromCache(String cacheKey, Class<T> clazz) {
        if (redisTemplate == null) return null;
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (json != null && !json.isBlank()) {
                log.info("【后台大盘】[缓存命中] Key: {}", cacheKey);
                return objectMapper.readValue(json, clazz);
            }
        } catch (Exception e) {
            log.warn("【后台大盘】[缓存读取异常] Key: {}", cacheKey, e);
        }
        return null;
    }

    private <T> List<T> readListFromCache(String cacheKey, Class<T> elementClazz) {
        if (redisTemplate == null) return null;
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (json != null && !json.isBlank()) {
                log.info("【后台大盘】[缓存命中] Key: {}", cacheKey);
                return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, elementClazz));
            }
        } catch (Exception e) {
            log.warn("【后台大盘】[缓存读取异常] Key: {}", cacheKey, e);
        }
        return null;
    }
}

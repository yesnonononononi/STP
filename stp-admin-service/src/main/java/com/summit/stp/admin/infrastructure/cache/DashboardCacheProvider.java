package com.summit.stp.admin.infrastructure.cache;

import java.util.List;
import java.util.function.Supplier;

/**
 * 后台大盘缓存操作 Provider 接口
 */
public interface DashboardCacheProvider {

    <T> T getOrSet(String cacheKey, Class<T> clazz, Supplier<T> supplier);

    <T> List<T> getOrSetList(String cacheKey, Class<T> elementClazz, Supplier<List<T>> supplier);
}

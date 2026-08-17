package com.summit.stp.common.util;

import com.summit.stp.common.application.domain.exception.DisTributeLockAcquireException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class DistributedLockUtil {
    private final RedissonClient redissonClient;

    public RLock getLock(String key) {
        return redissonClient.getLock(key);
    }

    /**
     * @param key 锁的key
     * @param task 任务
     * @param <T> 任务返回值类型
     * @return 任务返回值
     */
    public <T> T executeWithLock(String key, Supplier<T> task) {
        RLock lock = getLock(key);
        try {
            if (lock.tryLock()) {
                return task.get();
            }
            throw new DisTributeLockAcquireException("Failed to acquire lock");
        } finally {
            releaseLock(lock);
        }
    }

    /**
     * @param key 锁的key
     * @param waitTime 获取锁等待时间
     * @param leaseTime 锁有效期
     * @param task 带锁任务
     * @param <T> 任务返回值类型
     * @return 任务返回值
     */
    public <T> T executeWithLock(String key, long waitTime, long leaseTime, Supplier<T> task) {
        RLock lock = getLock(key);
        try {
            if (lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
                return task.get();
            }
            throw new DisTributeLockAcquireException("Failed to acquire lock");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            releaseLock(lock);
        }
    }

    /**
     * @param key 锁的key
     * @param task 带锁任务
     */
    public void executeWithLock(String key, Runnable task) {
        RLock lock = getLock(key);
        try {
            if (lock.tryLock()) {
                task.run();
                return;
            }
            throw new DisTributeLockAcquireException("Failed to acquire lock");
        } finally {
            releaseLock(lock);
        }
    }

    /**
     * @param key 锁的key
     * @param waitTime 获取锁等待时间
     * @param leaseTime 锁有效期
     * @param task 带锁任务
     */
    public void executeWithLock(String key, long waitTime, long leaseTime, Runnable task) {
        RLock lock = getLock(key);
        try {
            if (lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
                task.run();
                return;
            }
            throw new DisTributeLockAcquireException("Failed to acquire lock");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            releaseLock(lock);
        }
    }

    public void releaseLock(RLock lock) {
        if (lock != null && lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}

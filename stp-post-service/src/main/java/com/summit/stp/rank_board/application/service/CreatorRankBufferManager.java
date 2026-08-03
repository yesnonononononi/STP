package com.summit.stp.rank_board.application.service;

import com.summit.stp.post.infrastructure.constants.PostConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreatorRankBufferManager {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private ConcurrentHashMap<Long, LongAdder> scoreBuffer = new ConcurrentHashMap<>();

    /**
     * 增量记录创作者本地热度积分
     */
    public void incrementScore(Long creatorId, double score) {
        if (creatorId == null) {
            return;
        }
        scoreBuffer.computeIfAbsent(creatorId, k -> new LongAdder()).add((long) score);
    }

    /**
     * 定时刷写主入口：负责流程编排
     */
    @Scheduled(fixedRate = 5000)
    public void flushScheduledTask() {
        if (scoreBuffer.isEmpty()) {
            return;
        }
        // 1. 执行引用置换，清空当前 Map 并获取待刷写数据
        Map<Long, LongAdder> flushData = swapBuffer();
        
        // 2. 将数据批量更新到 Redis ZSet 中
        updateToRedis(flushData);
    }

    /**
     * 每日零点自动执行：对当前及前一周期的榜单进行裁剪（只保留 Top 100）
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cullScheduledTask() {
        log.info("【CreatorRank】自动任务 开始执行榜单裁剪");
        cullRankBoard();
        log.info("【CreatorRank】自动任务 榜单裁剪执行成功");
    }

    /**
     * 封装方法 1: 执行内存 Swap 操作
     */
    private Map<Long, LongAdder> swapBuffer() {
        ConcurrentHashMap<Long, LongAdder> oldBuffer = scoreBuffer;
        scoreBuffer = new ConcurrentHashMap<>();
        return oldBuffer;
    }

    /**
     * 封装方法 2: 批量更新 Redis Pipeline
     */
    @SuppressWarnings("unchecked")
    private void updateToRedis(Map<Long, LongAdder> data) {
        String zsetKey = getCurrentWeeklyZSetKey();
        long expireTimeEpoch = getSundayExpireTimeEpoch();
        
        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                data.forEach((creatorId, adder) -> {
                    double delta = adder.doubleValue();
                    operations.opsForZSet().incrementScore(zsetKey, creatorId, delta);
                });
                operations.expireAt(zsetKey, Instant.ofEpochSecond(expireTimeEpoch));
                return null;
            }
        });
        log.info("【CreatorRank】定时更新 批量刷新本地缓存到Redis成功");
    }

    /**
     * 封装方法 3: 榜单截断操作
     */
    private void cullRankBoard() {
        String zsetKey = getCurrentWeeklyZSetKey();
        redisTemplate.opsForZSet().removeRange(zsetKey, 0, -101);
    }

    /**
     * 封装方法 4: 获取当前周 ZSet Key
     */
    private String getCurrentWeeklyZSetKey() {
        return PostConstants.Cache.getCreatorWeeklyKey(LocalDate.now());
    }

    /**
     * 封装方法 5: 计算本周日 23:59:59 绝对过期秒级时间戳
     */
    private long getSundayExpireTimeEpoch() {
        LocalDate today = LocalDate.now();
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return sunday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}

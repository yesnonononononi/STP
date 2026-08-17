package com.summit.stp.rank_board.application.service;

import com.summit.stp.post.infrastructure.constants.PostConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreatorRankBufferManager {

    private final RedisTemplate<String, Object> redisTemplate;
    /**
     * 直写 Redis 更新创作者热度积分
     */
    public void incrementScore(Long creatorId, double score) {
        if (creatorId == null) {
            return;
        }
        String zsetKey = getCurrentWeeklyZSetKey();
        redisTemplate.opsForZSet().incrementScore(zsetKey, creatorId, score);
        redisTemplate.expireAt(zsetKey, Instant.ofEpochSecond(getSundayExpireTimeEpoch()));
        log.info("【CreatorRank】动作：直写Redis更新创作者积分成功, creatorId={}, score={}", creatorId, score);
    }

    /**
     * 每日零点自动执行：对当前榜单进行裁剪（只保留 Top 100）
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cullScheduledTask() {
        log.info("【CreatorRank】动作：开始执行榜单裁剪");
        cullRankBoard();
        log.info("【CreatorRank】动作：榜单裁剪执行成功");
    }

    /**
     * 榜单截断操作
     */
    private void cullRankBoard() {
        String zsetKey = getCurrentWeeklyZSetKey();
        redisTemplate.opsForZSet().removeRange(zsetKey, 0, -101);
    }

    /**
     * 获取当前周 ZSet Key
     */
    private String getCurrentWeeklyZSetKey() {
        return PostConstants.Cache.getCreatorWeeklyKey(LocalDate.now());
    }

    /**
     * 计算本周日 23:59:59 绝对过期秒级时间戳
     */
    private long getSundayExpireTimeEpoch() {
        LocalDate today = LocalDate.now();
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return sunday.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}


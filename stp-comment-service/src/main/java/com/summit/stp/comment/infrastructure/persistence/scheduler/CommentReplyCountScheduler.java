package com.summit.stp.comment.infrastructure.persistence.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.summit.stp.comment.infrastructure.constants.CommentConstants;
import com.summit.stp.comment.infrastructure.persistence.mapper.CommentsMapper;
import com.summit.stp.comment.infrastructure.persistence.po.CommentsPO;
import com.summit.stp.common.util.DistributedLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentReplyCountScheduler {
    private final CommentsMapper commentsMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DistributedLockUtil distributedLockUtil;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(cron = "0 0/8 * * * ?")
    public void updateCommentReplyCount() {
        // Tip: 分布式锁控制，防止多节点并发同时执行更新
        distributedLockUtil.executeWithLock(CommentConstants.Cache.REPLY_CHANGED_LOCK, this::conduct);
    }

    private void conduct() {
        // Tip: 1. 扫描并优先处理断电等突发状况下遗留下来的临时备份 Key
        Set<String> leftoverKeys = redisTemplate.keys(CommentConstants.Cache.REPLY_CHANGED_RUN_PREFIX + "*");
        if (leftoverKeys != null && !leftoverKeys.isEmpty()) {
            for (String keyObj : leftoverKeys) {
                if (keyObj != null) {
                    processBackupKey((String) keyObj);
                }
            }
        }

        // Tip: 2. 正常处理当前变更队列，采用 rename 转移至 UUID 临时 Key，保证原子操作
        String originalKey = CommentConstants.Cache.REPLY_CHANGED;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(originalKey))) {
            String backupKey = CommentConstants.Cache.REPLY_CHANGED_RUN_PREFIX + UUID.randomUUID();
            try {
                redisTemplate.rename(originalKey, backupKey);
                processBackupKey(backupKey);
            } catch (Exception ignored) {
                // Tip: 重命名失败说明已有其他实例在并行处理，可安全跳过
            }
        }
    }

    private void processBackupKey(String backupKey) {
        Set<Object> changedCommentIds = redisTemplate.opsForSet().members(backupKey);
        if (changedCommentIds == null || changedCommentIds.isEmpty()) {
            redisTemplate.delete(backupKey);
            return;
        }

        // Tip: 3. 解析并构建需要同步回复计数的评论 ID 列表
        List<Long> commentIds = changedCommentIds.stream()
                .map(this::toLong)
                .filter(value -> value != null)
                .toList();
        if (commentIds.isEmpty()) {
            redisTemplate.delete(backupKey);
            return;
        }

        try {
            // Tip: 4. 高性能批量查库：使用 SQL IN 批量统计各父评论的实际子评论数，绝对杜绝循环 N+1 查询
            List<Map<String, Object>> replyCountRows = commentsMapper.selectMaps(
                    new QueryWrapper<CommentsPO>()
                            .select("parent_id as parentId", "count(1) as replyCount")
                            .in("parent_id", commentIds)
                            .groupBy("parent_id")
            );

            // Tip: 5. 将数据库统计出来的分组结果存入临时 Map 备用
            Map<Long, Long> replyCountMap = new HashMap<>(commentIds.size());
            if (replyCountRows != null) {
                for (Map<String, Object> row : replyCountRows) {
                    Object parentIdValue = row.get("parentId");
                    Object replyCountValue = row.get("replyCount");
                    if (parentIdValue instanceof Number parentIdNumber) {
                        replyCountMap.put(parentIdNumber.longValue(), replyCountValue instanceof Number number ? number.longValue() : 0L);
                    }
                }
            }

            // Tip: 6. 在事务中批量更新对应评论的实际子评论条数
            transactionTemplate.executeWithoutResult(status -> {
                for (Long commentId : commentIds) {
                    LambdaUpdateWrapper<CommentsPO> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(CommentsPO::getId, commentId)
                            .set(CommentsPO::getReplyCount, replyCountMap.getOrDefault(commentId, 0L));
                    commentsMapper.update(null, updateWrapper);
                }
            });
            
            // Tip: 7. 彻底删除临时备份 Key
            redisTemplate.delete(backupKey);
            log.info("【定时评论回复数同步】已批量同步 {} 个评论回复数至数据库", commentIds.size());
        } catch (Exception e) {
            log.error("【定时评论回复数同步】批量同步失败 commentIds={}", commentIds, e);
        }
    }

    private Long toLong(Object obj) {
        if (obj instanceof Long value) return value;
        if (obj instanceof Integer value) return value.longValue();
        if (obj instanceof String value) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}

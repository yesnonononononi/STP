package com.summit.stp.post.infrastructure.persistence.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.infrastructure.persistence.PostRepositoryImpl;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import com.summit.stp.rank_board.application.service.RankCacheProvider;
import com.summit.stp.shared.util.DistributedLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostHotScoreScheduler {
    private final PostsMapper postsMapper;
    private final RankCacheProvider rankCacheProvider;
    private final DistributedLockUtil distributedLockUtil;
    private final TransactionTemplate transactionTemplate;
    private final PostRepositoryImpl postRepository;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void updatePostHotScore() {
        distributedLockUtil.executeWithLock("rank:lock:post_hot", this::conduct);
    }

    private void conduct() {
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            
            // 查询 7 天内处于正常公开状态的帖子
            LambdaQueryWrapper<PostsPO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PostsPO::getStatus, PostStatus.NORMAL.getCode())
                    .gt(PostsPO::getCreateTime, Timestamp.valueOf(sevenDaysAgo));
            
            List<PostsPO> activePosts = postsMapper.selectList(queryWrapper);
            if (activePosts == null || activePosts.isEmpty()) {
                log.info("【定时热度计算】最近7天无活跃帖子，无需计算");
                return;
            }
            
            log.info("【定时热度计算】开始计算 {} 个帖子的热度分数并更新缓存", activePosts.size());
            
            // 事务中批量更新数据库
            transactionTemplate.executeWithoutResult(status -> {
                for (PostsPO po : activePosts) {
                    Post post = postRepository.convertToDomain(po);
                    post.refreshHotScore();
                    double score = post.getHotScore() != null ? post.getHotScore() : 0.0;
                    
                    // 更新数据库
                    LambdaUpdateWrapper<PostsPO> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(PostsPO::getId, po.getId())
                            .set(PostsPO::getHotScore, (long) score);
                    postsMapper.update(null, updateWrapper);
                    
                    // 同步到 ZSet 缓存
                    rankCacheProvider.cachePostScore(post.getId(), score);
                }
            });
            log.info("【定时热度计算】帖子热度计算与缓存同步完成");
        } catch (Exception e) {
            log.error("【定时热度计算】更新热度失败", e);
        }
    }
}

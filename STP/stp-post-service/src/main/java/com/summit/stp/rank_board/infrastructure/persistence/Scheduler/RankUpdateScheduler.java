package com.summit.stp.rank_board.infrastructure.persistence.Scheduler;

import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.model.Tag;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.domain.repository.TagRepository;
import com.summit.stp.rank_board.application.service.RankCacheProvider;
import com.summit.stp.rank_board.domain.model.BoardType;
import com.summit.stp.rank_board.domain.model.RankBoard;
import com.summit.stp.rank_board.domain.repository.RankRepository;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.shared.util.DistributedLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RankUpdateScheduler {
    private final DistributedLockUtil distributedLockUtil;
    private final PostRepository postRepository;
    private final RankRepository rankRepository;
    private final TagRepository tagRepository;
    private final RankCacheProvider rankCacheProvider;

    @Scheduled(cron = "0 */3 * * * * ")
    public void execute() {
        distributedLockUtil.executeWithLock(PostConstants.Cache.RANK_LOCK, this::conduct);
    }

    private void conduct() {
        try {
            log.info("【排行榜更新-定时】start");
            List<Post> postList = postRepository.queryMostHotPost(20);
            updateCreatorRankBoard(postList);
            updatePostRankBoard(postList);

            List<Tag> tagList = tagRepository.queryTagByUseCount(20);
            updateTopicRankBoard(tagList);
        } catch (Exception e) {
            log.error("【排行榜更新-定时】更新排行榜失败", e);
        }
    }

    /**
     * 更新创建者排行榜
     */
    private void updateCreatorRankBoard(List<Post> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Long, Double> creatorScoreMap = list.stream()
                .filter(post -> post.getCreatorId() != null)
                .collect(Collectors.groupingBy(
                        Post::getCreatorId,
                        Collectors.summingDouble(post -> post.getHotScore() != null ? post.getHotScore() : 0.0)
                ));

        List<RankBoard> res = creatorScoreMap.entrySet().stream()
                .map(entry -> toDomain(entry.getKey(), null, entry.getValue(), 0, 1, BoardType.CREATOR.getType()))
                .sorted((o1, o2) -> o2.getScore().compareTo(o1.getScore()))
                .toList();

        for (int i = 0; i < res.size(); i++) {
            res.get(i).updateRank(i + 1);
        }
        rankRepository.insertCreatorRank(res);
        rankCacheProvider.updateCreatorRank(res);
    }

    /**
     * 更新热点帖子排行榜
     */
    private void updatePostRankBoard(List<Post> list) {
        List<RankBoard> res = list.stream()
                .map(post -> toDomain(post.getId(), post.getTitle(), post.getHotScore() != null ? post.getHotScore() : 0.0, 0, 1, BoardType.POST.getType()))
                .sorted((o1, o2) -> o2.getScore().compareTo(o1.getScore()))
                .toList();
        for (int i = 0; i < res.size(); i++) {
            res.get(i).updateRank(i + 1);
        }
        rankRepository.insertPostRank(res);
    }

    /**
     * 更新话题排行榜
     */
    private void updateTopicRankBoard(List<Tag> list) {
        List<RankBoard> res = list.stream()
                .map(tag -> toDomain(tag.getId(), tag.getTagName(), tag.getUseCount() != null ? tag.getUseCount().doubleValue() : 0.0, 0, 1, BoardType.TOPIC.getType()))
                .sorted((o1, o2) -> o2.getScore().compareTo(o1.getScore()))
                .toList();
        for (int i = 0; i < res.size(); i++) {
            res.get(i).updateRank(i + 1);
        }
        rankRepository.insertTopicRank(res);
    }

    private RankBoard toDomain(Long entityId, String entityName, double score, int rank, int status, String type) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monday = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        Timestamp weekStartDate = Timestamp.valueOf(monday);
        return RankBoard.builder()
                .entityId(entityId)
                .entityName(entityName)
                .score(score)
                .weekStartDate(weekStartDate)
                .rank(rank)
                .status(status)
                .updateTime(Timestamp.valueOf(now))
                .type(type)
                .build();
    }
}

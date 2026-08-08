package com.summit.stp.rank_board.application.service.impl;

import com.summit.stp.common.application.api.vo.UserSimpleVO;
import com.summit.stp.common.feign.UserFeignClient;
import com.summit.stp.common.result.Result;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.rank_board.application.service.RankCacheProvider;
import com.summit.stp.rank_board.application.service.RankService;
import com.summit.stp.rank_board.application.vo.CreatorRankVO;
import com.summit.stp.rank_board.application.vo.PostRankVO;
import com.summit.stp.rank_board.application.vo.TopicRankVO;
import com.summit.stp.rank_board.domain.model.BoardType;
import com.summit.stp.rank_board.domain.model.RankBoard;
import com.summit.stp.rank_board.domain.repository.RankRepository;
import com.summit.stp.tag.application.vo.TagVO;
import com.summit.stp.tag.domain.model.Tag;
import com.summit.stp.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class RankServiceImpl implements RankService {
    private final RankRepository repository;
    private final UserFeignClient userFeignClient;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final RankCacheProvider rankCacheProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Result<List<PostRankVO>> queryHotRankList(Integer size, Integer status) {
        // 1. 从防腐层获取缓存高热度帖子ID
        List<Long> postIds = rankCacheProvider.getHotPostIds(size);
        
        List<PostVO> postVOs;
        List<Long> orderedPostIds;
        Map<Long, Double> scoresMap;
        if (postIds.isEmpty()) {
            // 2. 缓存未命中，从数据库 post_rank 表读取快照，并预热数据到 Redis
            LocalDate currentMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            List<RankBoard> dbRanks = repository.queryPostRank(size, currentMonday);
            if (dbRanks.isEmpty()) {
                // 如果快照表也是空的，降级实时回源计算并预热
                List<Post> dbPosts = fallbackAndGetHotPosts(size);
                orderedPostIds = dbPosts.stream().map(Post::getId).toList();
                scoresMap = dbPosts.stream().collect(Collectors.toMap(
                    Post::getId,
                    post -> post.getHotScore() != null ? post.getHotScore() : 0.0
                ));
            } else {
                orderedPostIds = dbRanks.stream().map(RankBoard::getEntityId).toList();
                scoresMap = dbRanks.stream().collect(Collectors.toMap(
                    RankBoard::getEntityId,
                    RankBoard::getScore,
                    (v1, v2) -> v1
                ));
                prewarmPostCache(dbRanks);
            }
        } else {
            // 3. 缓存命中，批量获取帖子详情
            orderedPostIds = postIds;
            scoresMap = postIds.stream().collect(Collectors.toMap(
                id -> id,
                id -> {
                    Double s = rankCacheProvider.getPostScore(id);
                    return s != null ? s : 0.0;
                }
            ));
        }
        postVOs = hydratePostDetails(orderedPostIds, status);

        // 4. 构建并返回 PostRankVO 列表
        return Result.success(buildPostRankVOList(postVOs, orderedPostIds, scoresMap));
    }

    @Override
    public Result<List<TopicRankVO>> querySubjectRankList(Integer size, Integer status) {
        // 1. 从防腐层获取高使用次数的话题ID
        List<Long> tagIds = rankCacheProvider.getHotTopicIds(size);
        
        List<Tag> tags;
        List<Long> orderedTagIds;
        if (tagIds.isEmpty()) {
            // 2. 缓存未命中，从数据库 topic_rank 表读取快照，并预热数据到 Redis
            LocalDate currentMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            List<RankBoard> dbRanks = repository.queryTopicRank(size, currentMonday);
            if (dbRanks.isEmpty()) {
                // 如果快照表也是空的，降级实时回源计算并预热
                tags = fallbackAndGetHotTopics(size);
                orderedTagIds = tags.stream().map(Tag::getId).toList();
            } else {
                orderedTagIds = dbRanks.stream().map(RankBoard::getEntityId).toList();
                tags = hydrateTagDetails(orderedTagIds);
                prewarmTopicCache(dbRanks);
            }
        } else {
            // 3. 缓存命中，批量补齐详情
            orderedTagIds = tagIds;
            tags = hydrateTagDetails(orderedTagIds);
        }
        
        // 4. 构建并返回话题榜 TopicRankVO 列表
        return Result.success(buildTopicRankVOList(tags, orderedTagIds));
    }

    @Override
    public Result<List<CreatorRankVO>> queryCreatorRankList(Integer size, Integer status) {
        int limitSize = size > PostConstants.Business.CREATOR_MAX_DISPLAY_SIZE ? PostConstants.Business.CREATOR_MAX_DISPLAY_SIZE : size;
        String zsetKey = PostConstants.Cache.getCreatorWeeklyKey(LocalDate.now());
        Set<TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(zsetKey, 0, limitSize - 1);
        
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.success(fallbackQueryDb(limitSize));
        }

        List<Long> creatorIds = new ArrayList<>();
        List<Double> scores = new ArrayList<>();
        for (TypedTuple<Object> tuple : typedTuples) {
            if (tuple.getValue() != null) {
                creatorIds.add(((Number) tuple.getValue()).longValue());
                scores.add(tuple.getScore() != null ? tuple.getScore() : 0.0);
            }
        }
        
        if (creatorIds.isEmpty()) {
            return Result.success(fallbackQueryDb(limitSize));
        }
        
        Map<Long, UserSimpleVO> uMap = userFeignClient.findSimpleUserByIds(creatorIds).getData();
        List<CreatorRankVO> resultList = new ArrayList<>();
        Timestamp weekStart = Timestamp.valueOf(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay());
        
        int rank = 1;
        for (int i = 0; i < creatorIds.size(); i++) {
            Long creatorId = creatorIds.get(i);
            Double score = scores.get(i);
            UserSimpleVO uvo = uMap != null ? uMap.get(creatorId) : null;
            RankBoard rb = RankBoard.builder()
                    .entityId(creatorId)
                    .name("创作者周榜")
                    .score(score)
                    .rank(rank++)
                    .weekStartDate(weekStart)
                    .type(BoardType.CREATOR.getType())
                    .build();
            resultList.add(toCreatorVO(rb, uvo));
        }
        return Result.success(resultList);
    }

    /**
     * 创作者榜单查询的数据库降级兜底方法
     */
    private List<CreatorRankVO> fallbackQueryDb(Integer size) {
        LocalDate currentMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<RankBoard> resDb = repository.queryCreatorRank(size, currentMonday);
        List<Long> list = resDb.stream().map(RankBoard::getEntityId).toList();
        if (list.isEmpty()) {
            return List.of();
        }
        Map<Long, UserSimpleVO> uMap = userFeignClient.findSimpleUserByIds(list).getData();
        return resDb.stream()
                .distinct()
                .map(rb -> this.toCreatorVO(rb, uMap != null ? uMap.get(rb.getEntityId()) : null))
                .toList();
    }

    private void prewarmPostCache(List<RankBoard> ranks) {
        for (RankBoard rb : ranks) {
            rankCacheProvider.cachePostScore(rb.getEntityId(), rb.getScore());
        }
    }

    private void prewarmTopicCache(List<RankBoard> ranks) {
        for (RankBoard rb : ranks) {
            rankCacheProvider.incrementTopicScore(rb.getEntityId(), rb.getScore());
        }
    }

    private CreatorRankVO toCreatorVO(RankBoard rankBoard, @Nullable UserSimpleVO entity) {
        return CreatorRankVO.builder()
                .id(rankBoard.getId())
                .entityId(rankBoard.getEntityId())
                .rank(rankBoard.getRank())
                .entityInfo(entity)
                .name(rankBoard.getName())
                .score(rankBoard.getScore())
                .entityName(rankBoard.getEntityName())
                .week_start_date(rankBoard.getWeekStartDate())
                .type(rankBoard.getType())
                .bgImg(rankBoard.getBgImg())
                .build();
    }

    private PostRankVO toPostVO(RankBoard rankBoard, @Nullable PostVO entity) {
        return PostRankVO.builder()
                .id(rankBoard.getId())
                .entityId(rankBoard.getEntityId())
                .rank(rankBoard.getRank())
                .entityInfo(entity)
                .name(rankBoard.getName())
                .score(rankBoard.getScore())
                .entityName(rankBoard.getEntityName())
                .week_start_date(rankBoard.getWeekStartDate())
                .type(rankBoard.getType())
                .bgImg(rankBoard.getBgImg())
                .build();
    }

    private TopicRankVO toTopicVO(RankBoard rankBoard, @Nullable Tag entity) {
        return TopicRankVO.builder()
                .id(rankBoard.getId())
                .entityId(entity != null && entity.getId() != null ? entity.getId().toString() : null)
                .rank(rankBoard.getRank())
                .entityInfo(entity != null ? convertToVO(entity) : null)
                .name(rankBoard.getName())
                .score(rankBoard.getScore())
                .entityName(rankBoard.getEntityName())
                .week_start_date(rankBoard.getWeekStartDate())
                .type(rankBoard.getType())
                .bgImg(rankBoard.getBgImg())
                .build();
    }

    private TagVO convertToVO(Tag tag) {
        if (tag == null) {
            return null;
        }
        return TagVO.builder()
                .id(tag.getId() != null ? tag.getId().toString() : null)
                .tagName(tag.getTagName())
                .sort(tag.getSort())
                .useCount(tag.getUseCount())
                .status(tag.getStatus())
                .createTime(tag.getCreateTime())
                .build();
    }

    /**
     * 帖子排行榜缓存未命中时的数据库回源逻辑。
     * 从数据库中获取高热度的帖子，并将热度数据预热到防腐缓存中。
     *
     * @param size 请求榜单的数据长度上限
     * @return 从数据库中查得的高热度帖子列表
     */
    private List<Post> fallbackAndGetHotPosts(Integer size) {
        List<Post> dbPosts = postRepository.queryMostHotPost(size);
        if (dbPosts.isEmpty()) {
            return List.of();
        }
        rankCacheProvider.cacheHotPosts(dbPosts);
        return dbPosts;
    }

    /**
     * 批量查询并获取帖子的公开展示详情。
     *
     * @param postIds 帖子ID列表
     * @param status  帖子过滤状态
     * @return 帖子的 VO 详情列表
     */
    private List<PostVO> hydratePostDetails(List<Long> postIds, Integer status) {
        if (postIds.isEmpty()) {
            return List.of();
        }
        return postRepository.queryByPostIds(postIds, status, null);
    }

    /**
     * 根据已排列好的帖子 ID 序列与得分 Map，组装并生成帖子榜 PostRankVO 列表。
     *
     * @param postVOs        批量查询到的帖子展示层 VO 数据
     * @param orderedPostIds 按热度从高到低排列的帖子 ID 序列
     * @param scoresMap      帖子 ID 对应的热度分数值 Map
     * @return 最终排序的 PostRankVO 列表
     */
    private List<PostRankVO> buildPostRankVOList(List<PostVO> postVOs, List<Long> orderedPostIds, Map<Long, Double> scoresMap) {
        if (postVOs == null || postVOs.isEmpty()) {
            return List.of();
        }
        Map<Long, PostVO> voMap = postVOs.stream().collect(Collectors.toMap(PostVO::getId, v -> v));
        List<PostRankVO> result = new ArrayList<>();
        
        int rank = 1;
        for (Long postId : orderedPostIds) {
            PostVO pvo = voMap.get(postId);
            if (pvo == null) {
                continue;
            }
            Double score = scoresMap.get(postId);
            RankBoard rb = RankBoard.builder()
                    .entityId(pvo.getId())
                    .name("热点榜")
                    .score(score != null ? score : 0.0)
                    .rank(rank++)
                    .type(BoardType.POST.getType())
                    .build();
            result.add(this.toPostVO(rb, pvo));
        }
        return result;
    }

    /**
     * 话题榜缓存未命中时的数据库回源逻辑。
     * 从数据库中获取高使用次数的话题标签，并将数据预热到防腐缓存中。
     *
     * @param size 请求的数据条数
     * @return 回源查询到的话题列表
     */
    private List<Tag> fallbackAndGetHotTopics(Integer size) {
        List<Tag> dbTags = tagRepository.queryTagByUseCount(size);
        if (dbTags.isEmpty()) {
            return List.of();
        }
        rankCacheProvider.cacheHotTopics(dbTags);
        return dbTags;
    }

    /**
     * 批量查询并获取话题领域模型详情。
     *
     * @param tagIds 话题标签 ID 列表
     * @return 话题实体对象列表
     */
    private List<Tag> hydrateTagDetails(List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return List.of();
        }
        return tagRepository.findByIds(tagIds);
    }

    /**
     * 根据话题 ID 有序队列，组装并生成话题榜 TopicRankVO 列表。
     *
     * @param tags          话题的实体详情数据
     * @param orderedTagIds 有序的话题 ID 列表
     * @return 组装排序后的展示层 TopicRankVO 列表
     */
    private List<TopicRankVO> buildTopicRankVOList(List<Tag> tags, List<Long> orderedTagIds) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        Map<Long, Tag> tagMap = tags.stream().collect(Collectors.toMap(Tag::getId, v -> v));
        List<TopicRankVO> result = new ArrayList<>();
        
        int rank = 1;
        for (Long tagId : orderedTagIds) {
            Tag tag = tagMap.get(tagId);
            if (tag == null) {
                continue;
            }
            double score = tag.getUseCount() != null ? tag.getUseCount().doubleValue() : 0.0;
            RankBoard rb = RankBoard.builder()
                    .entityId(tagId)
                    .name("话题榜")
                    .score(score)
                    .rank(rank++)
                    .type(BoardType.TOPIC.getType())
                    .build();
            result.add(this.toTopicVO(rb, tag));
        }
        return result;
    }
}

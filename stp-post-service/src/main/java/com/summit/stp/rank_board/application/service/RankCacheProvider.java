package com.summit.stp.rank_board.application.service;

import com.summit.stp.post.domain.model.Post;
import com.summit.stp.rank_board.domain.model.RankBoard;
import com.summit.stp.tag.domain.model.Tag;

import java.util.List;
import java.util.Map;

/**
 * 排行榜缓存提供者接口，提供帖子、话题等排行榜在 Redis 缓存中的读取与写入操作
 */
public interface RankCacheProvider {
    
    /**
     * 从缓存中获取排名前 N 的热门帖子 ID 列表（分数从高到低排序）
     *
     * @param size 获取的元素数量
     * @return 热门帖子 ID 列表，如果无数据则返回空列表
     */
    List<Long> getHotPostIds(int size);

    /**
     * 将一批热门帖子及其热度得分批量写入 Redis 缓存 ZSet 中
     *
     * @param posts 热门帖子实体列表
     */
    void cacheHotPosts(List<Post> posts);

    /**
     * 从缓存中获取排名前 N 的热门话题（标签） ID 列表（根据使用频次从高到低排序）
     *
     * @param size 获取的元素数量
     * @return 热门话题 ID 列表，如果无数据则返回空列表
     */
    List<Long> getHotTopicIds(int size);

    /**
     * 将一批热门话题（标签）及其使用次数批量写入 Redis 缓存 ZSet 中
     *
     * @param tags 热门标签/话题列表
     */
    void cacheHotTopics(List<Tag> tags);

    /**
     * 帖子分数增加
     *
     * @param tagId 话题id
     * @param delta  分数增量
     */
    void incrementTopicScore(Long tagId, double delta);

    /**
     * 缓存或更新单个帖子的热度得分，并为该 ZSet 键设置 7 天的过期时间
     *
     * @param postId 帖子 ID
     * @param score 帖子计算得到的热度分数
     */
    void cachePostScore(Long postId, double score);

    void cachePostsScore(Map<Long,Double> posts);

    /**
     * 获取单个帖子在 Redis 缓存中的热度得分
     *
     * @param postId 帖子 ID
     * @return 帖子的热度得分，若缓存中不存在或入参为空则返回 null
     */
    Double getPostScore(Long postId);

    void updateCreatorRank(List<RankBoard> res);
    void cacheCreatorRank(List<RankBoard> res);
}


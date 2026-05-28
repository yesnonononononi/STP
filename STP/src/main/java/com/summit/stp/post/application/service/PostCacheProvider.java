package com.summit.stp.post.application.service;

import java.util.List;
import java.util.Map;

public interface PostCacheProvider {
    final String COLLECT = "COLLECT";
    final String LIKE = "LIKE";

    /**
     * 点赞
     *
     * @param postId 帖子id
     * @param userId 用户id
     */
    void like(Long postId, Long userId);

    /**
     * 收藏
     *
     * @param postId 帖子id
     * @param userId 用户id
     */
    void collect(Long postId, Long userId);

    /**
     * 判断该用户是否收藏
     */
    boolean isCollected(Long postId, Long userId);


    /**
     * 获取单个帖子的点赞数
     */
    Long getLikeCount(Long postId);


    /**
     * 获取单个帖子的收藏数
     */
    Long getCollectCount(Long postId);

    /**
     * 判断该用户是否点赞
     */
    boolean isLiked(Long postId, Long userId);

    /**
     * 加载缓存
     */
    void loadCache(Long postId);

    /**
     * 批量加载缓存
     */
    void loadCache(java.util.List<Long> postIds);

    /**
     * 批量获取帖子点赞数与收藏数
     *
     * @param postIdList 帖子id列表
     * @return 帖子id -> (点赞数&收藏数)
     */
    Map<Long, Map<String, Long>> getLikeAndCollectCount(List<Long> postIdList);

    /**
     * 批量获取当前用户是否点赞帖子或者收藏帖子
     */
    Map<Long, Map<String, Boolean>> getIsCollectedOrLiked(List<Long> postIdList, Long userId);

    /**
     * 获取被修改的缓存列表
     */
    List<Object> getChangedList();
    /**
     * 标记缓存被修改
     */
    void markCacheChanged(Object postId);
    /**
     * 获取指定帖子所有点赞的用户ID列表（排除了占位符）
     */
    java.util.Set<Long> getLikeUserIds(Long postId);
    /**
     * 获取指定帖子所有收藏的用户ID列表（排除了占位符）
     */
    java.util.Set<Long> getCollectUserIds(Long postId);
}

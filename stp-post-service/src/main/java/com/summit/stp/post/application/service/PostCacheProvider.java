package com.summit.stp.post.application.service;

import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.domain.model.Post;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 帖子缓存服务接口
 */
public interface PostCacheProvider {


    /**
     * 点赞（已点赞即取消）
     *
     * @return
     */
    boolean like(Long postId, Long userId);

    /**
     * 收藏（已收藏即取消）
     *
     * @return
     */
    boolean collect(Long postId, Long userId);

    /**
     * 判断该用户是否收藏
     */
    boolean isCollected(Long postId, Long userId);

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
    void loadCache(List<Long> postIds);

    /**
     * 批量获取帖子点赞数与收藏数（直接返回真实值，无偏移量）
     *
     * @return 帖子id -> (字段名 -> 计数)
     */
    Map<Long, Map<String, Long>> getLikeAndCollectCount(List<Long> postIdList);

    /**
     * 批量获取当前用户是否点赞/收藏帖子
     */
    Map<Long, Map<String, Boolean>> getIsCollectedOrLiked(List<Long> postIdList, Long userId);

    void markChanged(Object postId);

    /**
     * 增加浏览数
     */
    void incrViewCount(Long postId);

    /**
     * 批量获取浏览数
     */
    Map<Long, Long> getViewCounts(List<Long> postIds);

    /**
     * 批量获取点赞用户ID列表
     */
    Map<Long, Set<Long>> batchGetLikeUserIds(List<Long> postIdList);

    Map<Long, Set<Long>> batchGetCollectUserIds(List<Long> postIdList);

    /**
     * 增加回复数
     */
    void incrReplyCount(Long postId);

    /**
     * 减少回复数
     */
    void decrReplyCount(Long postId);

    /**
     * 批量获取回复数
     */
    Map<Long, Long> getReplyCounts(List<Long> postIds);

    /**
     * 将帖子加入最新发布 ZSet 缓存
     */
    void addToNewestZSet(Long postId);

    /**
     * 将帖子加入热门 ZSet 缓存
     */
    void addToHotZSet(Long postId, double score);

    /**
     * 批量将帖子加入热门 ZSet 缓存
     */
    void addToHotZSet(Map<Long, Double> scoresMap);


    /**
     * 从最新 ZSet 缓存中按偏移量分页获取帖子ID（降序）
     */
    Set<Long> getPostIdsFromNewest(int offset, int limit);

    /**
     * 从热门 ZSet 缓存中按偏移量分页获取帖子ID（降序）
     */
    Set<Long> getPostIdsFromHot(int offset, int limit);

    /**
     * 批量重建热门帖子 ZSet 缓存
     */
    void rebuildHotZSet(Map<Long, Double> posts);

    /**
     * 重建最新帖子 ZSet 缓存（懒加载触发：ZSet 为空时从 DB 拉取最近帖子填充）
     */
    void rebuildNewestZSet();

    /**
     * 获取帖子在热门列表中的排名（降序）
     */
    Long getRankFromHot(Long postId);

    /**
     * 获取帖子在最新列表中的排名（降序）
     */
    Long getRankFromNewest(Long postId);

    /**
     * 获取帖子本体内容缓存
     */
    PostVO getPostContent(Long postId);

    /**
     * 批量获取帖子本体内容缓存（仅返回命中的）
     */
    Map<Long, PostVO> batchGetPostContents(List<Long> postIds);

    /**
     * 批量获取帖子的 tagIds 字段（从 detail Hash 读取，逗号分隔字符串，缺失的 postId 不在返回 Map 中）
     */
    Map<Long, String> batchGetTagIds(List<Long> postIds);

    /**
     * 扫描变更备份集合 Key 列表
     */
    Set<String> scanChangedRunKeys();

    /**
     * 判断是否存在变更集合 Key
     */
    boolean hasChangedKey();

    /**
     * 将变更集合 Key 重命名为备份 Key
     */
    boolean renameChangedKey(String backupKey);

    /**
     * 获取备份 Key 中的帖子 ID 集合
     */
    Set<Long> getBackupPostIds(String backupKey);

    /**
     * 删除备份 Key
     */
    void deleteBackupKey(String backupKey);

    /**
     * 从备份 Key 中移除已处理的帖子 ID
     */
    void removeBackupPostIds(String backupKey, List<Long> postIds);

    /**
     * 批量更新写回帖子 Detail Hash 缓存
     */
    void batchCachePostDetail(List<Post> posts);

    /**
     * 保存帖子本体内容缓存
     */
    void savePostContent(PostVO vo);

    /**
     * 删除帖子本体内容缓存
     */
    void deletePostContent(Long postId);

    /**
     * 写入帖子状态缓存
     */
    void cachePostStatus(Long postId, int status);

    /**
     * 从热门/最新 ZSet 缓存中移除帖子
     */
    void removeFromQueryZSets(Long postId);


    /**
     * 获取活跃的帖子
     * @return 活跃的帖子列表
     */
    List<Long> getActivePosts();

    /**
     * 添加活跃的帖子
     * @param postId 帖子id
     */
    void putActive(Long postId);

}

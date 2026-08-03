package com.summit.stp.post.application.service;

import com.summit.stp.post.api.dto.request.QueryPostListPageRequest;
import com.summit.stp.post.application.vo.PostVO;

import java.util.List;

/**
 * 帖子查询服务（CQRS 读模型服务），解耦领域层仓储，专注于面向展现的分页、连表查询
 */
public interface PostQueryService {
    /**
     * 分页获取帖子列表
     * @param cursor 分页游标
     * @param self 是否只查询自己
     * @param creatorId 创建者ID
     * @param status 帖子状态
     * @param orderType 排序类型 (newest/hot)
     * @param limit 每页数量
     * @return 帖子列表
     */
    List<PostVO> getPostPage(Long cursor, Boolean self, Long creatorId, Integer status, String orderType, Integer limit);

    List<PostVO> getByPostIds(List<Long> ids);

    /**
     * 获取用户收藏的帖子列表
     * @param targetUserId 目标用户ID
     * @param cursor 分页游标
     * @return 帖子列表
     */
    List<PostVO> getMyCollectPostList(Long targetUserId, String cursor);
    /**
     * 获取用户点赞的帖子列表
     *
     * @param targetUserId 目标用户ID
     * @param cursor       分页游标
     * @return 帖子列表
     */
    List<PostVO> getMyLikePostList(Long targetUserId, String cursor);
    /**
     * 获取用户关注的帖子列表
     *
     * @param request 查询请求参数
     * @return 帖子列表
     */
    List<PostVO> getFollowPostList(QueryPostListPageRequest request);
    PostVO findById(Long id, Long uid, Integer status);

    /**
     * 搜索帖子
     *
     * @param keyWord 关键词
     * @return 帖子列表
     */
    List<PostVO> searchPost(String keyWord);
}

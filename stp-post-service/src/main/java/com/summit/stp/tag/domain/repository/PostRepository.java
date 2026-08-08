package com.summit.stp.tag.domain.repository;


import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.domain.model.Post;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository {

    Post save(Post post);
    void update(Post post);

    List<PostVO> queryByPostIds(List<Long> posts,Integer status,Long userId);

    Post findById(Long id);

    /**
     * 查询最热门的帖子
     * @param limit
     * @return
     */
    List<Post> queryMostHotPost(int limit);

    List<Post> queryPostsByDay(LocalDateTime sevenDaysAgo);

    void updateBatchById(List<Post> list);

    /**
     * 分页查询帖子（游标分页降级用）
     *
     * @param cursor 游标帖子ID
     * @param creatorId 创作者ID
     * @param userId 当前用户ID
     * @param status 帖子状态
     * @param limit 每页数量
     * @return 帖子VO列表
     */
    List<PostVO> queryByPage(Long cursor, Long creatorId, Long userId, Integer status, Integer limit);

    List<Long> getPostsByTag(Long tagId, String cursor, Integer limit);
}

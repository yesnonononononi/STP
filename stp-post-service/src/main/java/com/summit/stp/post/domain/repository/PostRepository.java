package com.summit.stp.post.domain.repository;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.domain.model.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository {

    void save(Post post);
    void update(Post post);

    List<PostVO> queryByPostIds(List<Long> posts,Integer status,Long userId);

    List<Post> findByIds(List<Long> ids);
    Optional<Post> findById(Long id);

    /**
     * 查询最热门的帖子
     * @param limit
     * @return
     */
    List<Post> queryMostHotPost(int limit);

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

    List<Long> getHotPostsByTag(Long tagId, String cursor, Integer limit);
}

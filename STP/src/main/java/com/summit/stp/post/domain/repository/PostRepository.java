package com.summit.stp.post.domain.repository;


import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.domain.model.Post;

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
}

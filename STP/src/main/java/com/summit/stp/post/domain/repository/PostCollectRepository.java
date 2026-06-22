package com.summit.stp.post.domain.repository;

import com.summit.stp.post.infrastructure.persistence.po.PostCollectPO;

import java.util.List;
import java.util.Map;

public interface PostCollectRepository {
    void save(PostCollectPO postCollect);
    void delete(Long postId, Long userId);
    boolean exists(Long postId, Long userId);
    long countByPostId(Long postId);
    List<Long> findUserIdsByPostId(Long postId);
    Map<Long, List<Long>> findUserIdsByPostIds(List<Long> postIds);

    List<Long> findByUserId(Long userId,String cursor);
}

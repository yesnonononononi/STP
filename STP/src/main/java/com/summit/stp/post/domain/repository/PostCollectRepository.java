package com.summit.stp.post.domain.repository;

import com.summit.stp.post.infrastructure.persistence.po.PostCollectPO;

public interface PostCollectRepository {
    void save(PostCollectPO postCollect);
    void delete(Long postId, Long userId);
    boolean exists(Long postId, Long userId);
    long countByPostId(Long postId);
    java.util.List<Long> findUserIdsByPostId(Long postId);
    java.util.Map<Long, java.util.List<Long>> findUserIdsByPostIds(java.util.List<Long> postIds);
}

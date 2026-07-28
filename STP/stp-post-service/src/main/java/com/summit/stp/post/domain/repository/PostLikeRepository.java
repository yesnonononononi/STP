package com.summit.stp.post.domain.repository;

import com.summit.stp.post.infrastructure.persistence.po.PostLikePO;
import java.util.List;
import java.util.Map;

public interface PostLikeRepository {
    void save(PostLikePO postLike);
    void delete(Long postId, Long userId);
    boolean exists(Long postId, Long userId);
    long countByPostId(Long postId);
    java.util.List<Long> findUserIdsByPostId(Long postId);
    Map<Long,List<Long>> findUserIdsByPostIds(List<Long> postIds);
    List<Long> findByUserId(Long userId, String cursor);


    void batchSave(List<PostLikePO> toAddList);

    void batchDelete(List<Long[]> toRemoveList);
}


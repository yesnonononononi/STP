package com.summit.stp.post.domain.repository;

import com.summit.stp.post.application.vo.PostImageVO;
import com.summit.stp.post.domain.model.PostImage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PostImageRepository {

    List<PostImage> findByPostId(Long postId);
    void save(PostImage postImage);
    void delete(Long id);
    void deleteByPostId(Long postId);
    Long countByPostId(Long postId);
    Map<Long, List<PostImageVO>> findByIds(List<Long> postIds);

    void batchSave(List<PostImage> urls);
    Optional<PostImage> findById(Long id);
}

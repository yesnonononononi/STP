package com.summit.stp.tag.domain.repository;

import com.summit.stp.tag.domain.model.PostTag;

import java.util.List;

public interface PostTagRelRepository {
    void save(PostTag rel);
    void delete(Long id);
    void deleteByPostId(Long postId);
    List<PostTag> findByPostId(Long postId);
    List<PostTag> findByTagId(Long tagId);
    List<PostTag> findByPostIds(List<Long> postIds);

    void batchSave(Long postId, List<Long> tags);
}

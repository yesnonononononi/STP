package com.summit.stp.post.domain.repository;

import com.summit.stp.post.application.vo.PostTagRelVO;
import com.summit.stp.post.domain.model.PostTag;
import com.summit.stp.post.infrastructure.persistence.po.PostTagRelPO;
import java.util.List;

public interface PostTagRelRepository {
    void save(PostTagRelPO rel);
    void delete(Long id);
    void deleteByPostId(Long postId);
    List<PostTagRelVO> findByPostId(Long postId);
    List<PostTagRelVO> findByTagId(Long tagId);
    List<PostTagRelVO> findByPostIds(List<Long> postIds);

    void batchSave(Long postId,List<Long> tags);

}

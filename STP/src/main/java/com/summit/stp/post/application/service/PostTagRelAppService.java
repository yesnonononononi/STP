package com.summit.stp.post.application.service;

import com.summit.stp.post.application.vo.PostTagRelVO;
import com.summit.stp.post.infrastructure.persistence.po.PostTagRelPO;
import java.util.List;

public interface PostTagRelAppService {
    void bindTag(Long postId, List<Long> tagIds);
    void unbindTag(Long id);
    void clearPostTags(Long postId);
    List<PostTagRelVO>  getRelationsByPostId(Long postId);
    List<PostTagRelVO>  getRelationsByTagId(Long tagId);
}

package com.summit.stp.tag.application.service;

import com.summit.stp.tag.application.vo.PostTagRelVO;

import java.util.List;

public interface PostTagRelAppService {
    void bindTag(Long postId, List<Long> tagIds);
    void unbindTag(Long id);
    void clearPostTags(Long postId);
    List<PostTagRelVO>  getRelationsByPostId(Long postId);
    List<PostTagRelVO>  getRelationsByTagId(String tagUuid);
}

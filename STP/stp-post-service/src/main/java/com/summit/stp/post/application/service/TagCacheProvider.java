package com.summit.stp.post.application.service;

import java.util.List;

public interface TagCacheProvider {
    List<Long> getPostsHotTag(Long tagId, String cursor, Integer limit);

    List<Long> getPostsTag(Long tagId, String cursor, Integer limit);

    void addPostToTags(Long postId, List<Long> tagIds);

    void removePostFromTags(Long postId, List<Long> tagIds);
}

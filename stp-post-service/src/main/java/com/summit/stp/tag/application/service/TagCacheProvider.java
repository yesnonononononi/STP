package com.summit.stp.tag.application.service;

import com.summit.stp.tag.application.vo.TagVO;

import java.util.List;
import java.util.Map;

public interface TagCacheProvider {
    List<Long> getPostsHotTag(Long tagId, String cursor, Integer limit);

    List<Long> getPostsTag(Long tagId, String cursor, Integer limit);

    void addPostToTags(Long postId, List<Long> tagIds);

    void removePostFromTags(Long postId, List<Long> tagIds);

    /**
     * 批量获取标签详情缓存（仅返回命中的）
     */
    Map<Long, TagVO> batchGetTagDetails(List<Long> tagIds);

    /**
     * 批量保存标签详情缓存
     */
    void batchSaveTagDetails(List<TagVO> tags);

    /**
     * 删除单个标签详情缓存
     */
    void deleteTagDetail(Long tagId);
}


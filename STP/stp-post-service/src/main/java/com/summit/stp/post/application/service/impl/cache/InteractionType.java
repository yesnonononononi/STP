package com.summit.stp.post.application.service.impl.cache;

import com.summit.stp.shared.constants.CacheFieldConstants;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import lombok.Getter;

/**
 * 帖子互动类型枚举（点赞/收藏），用于统一模板化消除对称重复代码
 */
@Getter
public enum InteractionType {

    LIKE(PostConstants.Cache.LIKE_SET_PREFIX, CacheFieldConstants.LIKE_COUNT),
    COLLECT(PostConstants.Cache.COLLECT_SET_PREFIX, CacheFieldConstants.COLLECT_COUNT);

    /** Redis Set key 前缀 */
    private final String keyPrefix;

    /** 帖子 Hash 中对应的计数字段名 */
    private final String countField;

    InteractionType(String keyPrefix, String countField) {
        this.keyPrefix = keyPrefix;
        this.countField = countField;
    }

    public String buildSetKey(Long postId) {
        return keyPrefix + postId;
    }
}

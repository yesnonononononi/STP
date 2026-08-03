package com.summit.stp.post.application.service.impl.cache;

import com.summit.stp.post.application.vo.TagVO;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 标签详情缓存操作，按 tagId 独立缓存，多个帖子共享同一 tag 缓存。
 * key: tag:detail:<tagId>，value: TagVO JSON
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TagDetailCacheOps {

    private static final long BASE_EXPIRE_SECONDS = 604800L;
    private static final int RANDOM_OFFSET_BOUND = 86400;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;


    /**
     * 批量获取标签详情缓存，仅返回命中的
     */
    public Map<Long, TagVO> batchGetTagDetails(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, TagVO> result = new HashMap<>(tagIds.size());
        try {
            List<Object> pipelineResults = redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public Object execute(@NonNull RedisOperations operations) {
                    for (Long tagId : tagIds) {
                        operations.opsForValue().get(PostConstants.Cache.TAG_DETAIL_PREFIX + tagId);
                    }
                    return null;
                }
            });
            for (int i = 0; i < tagIds.size(); i++) {
                Object raw = pipelineResults.get(i);
                if (raw != null) {
                    TagVO tag = deserialize(raw.toString());
                    if (tag != null) {
                        result.put(tagIds.get(i), tag);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("【帖子模块】批量获取标签详情缓存异常", e);
        }
        return result;
    }

    /**
     * 批量保存标签详情缓存（Pipeline）
     */
    public void batchSaveTagDetails(List<TagVO> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) {
                for (TagVO tag : tags) {
                    String key = PostConstants.Cache.TAG_DETAIL_PREFIX + tag.getId();
                    operations.opsForValue().set(key, serialize(tag), randomExpire(), TimeUnit.SECONDS);
                }
                return null;
            }
        });
    }

    /**
     * 删除单个标签缓存
     */
    public void deleteTagDetail(Long tagId) {
        try {
            redisTemplate.delete(PostConstants.Cache.TAG_DETAIL_PREFIX + tagId);
        } catch (Exception e) {
            log.warn("【帖子模块】删除标签详情缓存异常，tagId={}", tagId, e);
        }
    }

    private String serialize(TagVO tag) {
        try {
            return objectMapper.writeValueAsString(tag);
        } catch (Exception e) {
            log.warn("【帖子模块】序列化标签详情异常", e);
            return "{}";
        }
    }

    private TagVO deserialize(String json) {
        try {
            return objectMapper.readValue(json, TagVO.class);
        } catch (Exception e) {
            log.warn("【帖子模块】反序列化标签详情异常", e);
            return null;
        }
    }

    private long randomExpire() {
        return BASE_EXPIRE_SECONDS + ThreadLocalRandom.current().nextInt(RANDOM_OFFSET_BOUND);
    }
}

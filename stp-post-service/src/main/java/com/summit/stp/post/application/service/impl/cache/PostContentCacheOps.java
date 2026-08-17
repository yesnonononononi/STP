package com.summit.stp.post.application.service.impl.cache;

import com.summit.stp.common.constants.CacheFieldConstants;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 帖子内容 Hash 缓存操作（帖子主体信息的读写）
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class PostContentCacheOps {

    /** 基础过期时间：7 天（秒） */
    private static final long BASE_EXPIRE_SECONDS = 604800L;
    /** 随机偏移上限：1 天（秒），防止缓存雪崩 */
    private static final int RANDOM_OFFSET_BOUND = 86400;

    private final RedisTemplate<String, Object> redisTemplate;

    public PostContentCacheOps(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 从缓存获取帖子内容
     */
    public PostVO getPostContent(Long postId) {
        try {
            String key = PostConstants.Cache.DETAIL_PREFIX + postId;
            Map<Object, Object> fields = redisTemplate.opsForHash().entries(key);
            if (fields.isEmpty()) {
                return null;
            }
            return mapToPostVO(postId, fields);
        } catch (Exception e) {
            log.warn("【帖子模块】获取帖子内容缓存异常，动作：获取帖子内容缓存", e);
            return null;
        }
    }

    /**
     * 批量获取帖子的 tagIds 字段（从 detail Hash 读取，逗号分隔）
     */
    public Map<Long, String> batchGetTagIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> result = new HashMap<>(postIds.size());
        try {
            for (Long postId : postIds) {
                Object raw = redisTemplate.opsForHash().get(PostConstants.Cache.DETAIL_PREFIX + postId, CacheFieldConstants.TAG_IDS);
                if (raw != null) {
                    result.put(postId, raw.toString());
                }
            }
        } catch (Exception e) {
            log.warn("【帖子模块】批量获取帖子tagIds缓存异常", e);
        }
        return result;
    }

    /**
     * 批量获取帖子内容缓存（仅返回命中的，缺失的不会出现在结果 Map 中）
     */
    public Map<Long, PostVO> batchGetPostContents(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, PostVO> result = new HashMap<>(postIds.size());
        try {
            List<Object> maps = redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    for (Long postId : postIds) {
                        redisTemplate.opsForHash().entries(PostConstants.Cache.DETAIL_PREFIX + postId);
                    }
                    return null;
                }
            });
            for (int i = 0; i < maps.size(); i++) {
                Object map = maps.get(i);
                if(map instanceof  Map m && !m.isEmpty()){
                    result.put(postIds.get(i), mapToPostVO(postIds.get(i), m));
                }else{
                    log.error("【帖子模块】批量获取帖子内容缓存异常:{}", map.toString());
                }
            }

        } catch (Exception e) {
            log.warn("【帖子模块】批量获取帖子内容缓存异常", e);
        }
        return result;
    }

    /**
     * 保存帖子内容到缓存
     */
    public void savePostContent(PostVO vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        try {
            String key = PostConstants.Cache.DETAIL_PREFIX + vo.getId();
            Map<String, Object> fields = buildHashFields(
                    vo.getCreatorId(), vo.getTitle(), vo.getType(), vo.getContent(),
                    vo.getStatus(), vo.getCreateTime(), vo.getUpdateTime(),
                    vo.getIsTop(), vo.getVisibleScope(),
                    vo.getLikeCount(), vo.getCollectCount(), vo.getReplyCount(), vo.getViewCount(),
                    vo.getExtraMediaUrl(), null
            );
            redisTemplate.opsForHash().putAll(key, fields);
            redisTemplate.expire(key, randomExpire(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("【帖子模块】保存帖子内容缓存异常，动作：保存帖子内容缓存", e);
        }
    }

    /**
     * 删除帖子内容缓存
     */
    public void deletePostContent(Long postId) {
        try {
            redisTemplate.delete(PostConstants.Cache.DETAIL_PREFIX + postId);
        } catch (Exception e) {
            log.warn("【帖子模块】删除帖子内容缓存异常，动作：删除帖子内容缓存", e);
        }
    }

    /**
     * 缓存帖子状态
     */
    public void cachePostStatus(Long postId, int status) {
        try {
            String key = "post:status:" + postId;
            redisTemplate.opsForValue().set(key, String.valueOf(status), 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("【帖子模块】写入帖子状态缓存异常，动作：写入帖子状态缓存", e);
        }
    }

    /**
     * 批量加载缺失的帖子 Hash 缓存（Pipeline 预热）
     */
    public void batchLoadMissingPostHash(
            List<Long> missingPostIds,
            List<PostsPO> poList,
            Map<Long, List<Long>> tagIdsMap
    ) {
        if (missingPostIds.isEmpty() || poList.isEmpty()) {
            return;
        }
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) {
                for (PostsPO po : poList) {
                    Long pid = po.getId();
                    String likeSetKey = InteractionType.LIKE.buildSetKey(pid);
                    String collectSetKey = InteractionType.COLLECT.buildSetKey(pid);
                    Long zsetLikeSize = redisTemplate.opsForZSet().zCard(likeSetKey);
                    Long zsetCollectSize = redisTemplate.opsForZSet().zCard(collectSetKey);
                    // 扣除预热标志占位 -1L 后的真实大小
                    long liveLikeSize = (zsetLikeSize != null && zsetLikeSize > 0) ? (redisTemplate.opsForZSet().score(likeSetKey, -1L) != null ? zsetLikeSize - 1 : zsetLikeSize) : 0L;
                    long liveCollectSize = (zsetCollectSize != null && zsetCollectSize > 0) ? (redisTemplate.opsForZSet().score(collectSetKey, -1L) != null ? zsetCollectSize - 1 : zsetCollectSize) : 0L;

                    long likeCount = Math.max(liveLikeSize, po.getLikeCount() != null ? po.getLikeCount() : 0L);
                    long collectCount = Math.max(liveCollectSize, po.getCollectCount() != null ? po.getCollectCount() : 0L);

                    List<Long> tagIds = tagIdsMap.getOrDefault(pid, Collections.emptyList());
                    Map<String, Object> fields = buildHashFieldsFromPO(po, likeCount, collectCount, tagIds);
                    String postKey = PostConstants.Cache.DETAIL_PREFIX + pid;
                    operations.opsForHash().putAll(postKey, fields);
                    operations.expire(postKey, randomExpire(), TimeUnit.SECONDS);
                }

                return null;
            }
        });
    }

    /**
     * 检查帖子 Hash 是否存在
     */
    public boolean exists(Long postId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PostConstants.Cache.DETAIL_PREFIX + postId));
    }

    /**
     * 从 PO 构建帖子 Hash 数据（用于单条预热）
     */
    public Map<String, Object> buildHashFieldsFromPO(PostsPO po, long likeCount, long collectCount, List<Long> tagIds) {
        return buildHashFields(
                po.getCreatorId(), po.getTitle(), po.getType(), po.getContent(),
                po.getStatus(), po.getCreateTime(), po.getUpdateTime(),
                po.getIsTop(), po.getVisibleScope(), likeCount, collectCount,
                po.getReplyCount() != null ? po.getReplyCount().longValue() : null,
                po.getViewCount(),
                po.getMediaUrls(),
                tagIds
        );
    }

    /**
     * 单条帖子 Hash 预热（检查存在性后写入）
     */
    public void loadSinglePostHash(Long postId, PostsPO po, long likeCount, long collectCount, List<Long> tagIds) {
        String postKey = PostConstants.Cache.DETAIL_PREFIX + postId;
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(postKey))) {
            Map<String, Object> fields = buildHashFieldsFromPO(po, likeCount, collectCount, tagIds);
            redisTemplate.opsForHash().putAll(postKey, fields);
            redisTemplate.expire(postKey, randomExpire(), TimeUnit.SECONDS);
        }
    }

    // ======================== 内部方法 ========================

    Map<String, Object> buildHashFields(
            Long creatorId, String title, Integer type, String content, Integer status,
            Timestamp createTime, Timestamp updateTime, Integer isTop, Integer visibleScope,
            Long likeCount, Long collectCount, Long replyCount, Long viewCount,
            String extraMediaUrl, List<Long> tagIds
    ) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(CacheFieldConstants.CREATOR_ID, String.valueOf(creatorId));
        fields.put(CacheFieldConstants.TITLE, title != null ? title : "");
        fields.put(CacheFieldConstants.TYPE, type != null ? String.valueOf(type) : "1");
        fields.put(CacheFieldConstants.CONTENT, content != null ? content : "");
        fields.put(CacheFieldConstants.STATUS, status != null ? String.valueOf(status) : "1");
        fields.put(CacheFieldConstants.CREATE_TIME, createTime != null ? String.valueOf(createTime.getTime()) : "0");
        fields.put(CacheFieldConstants.UPDATE_TIME, updateTime != null ? String.valueOf(updateTime.getTime()) : "0");
        fields.put(CacheFieldConstants.IS_TOP, isTop != null ? String.valueOf(isTop) : "0");
        fields.put(CacheFieldConstants.VISIBLE_SCOPE, visibleScope != null ? String.valueOf(visibleScope) : "1");
        fields.put(CacheFieldConstants.LIKE_COUNT, likeCount != null ? likeCount : 0L);
        fields.put(CacheFieldConstants.COLLECT_COUNT, collectCount != null ? collectCount : 0L);
        fields.put(CacheFieldConstants.REPLY_COUNT, replyCount != null ? replyCount : 0L);
        fields.put(CacheFieldConstants.VIEW_COUNT, viewCount != null ? viewCount : 0L);
        fields.put(CacheFieldConstants.EXTRA_MEDIA_URL, extraMediaUrl != null ? extraMediaUrl : "");
        fields.put(CacheFieldConstants.TAG_IDS, tagIds != null && !tagIds.isEmpty()
                ? tagIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("") : "");
        return fields;
    }

    private PostVO mapToPostVO(Long postId, Map<Object, Object> fields) {
        return PostVO.builder()
                .id(postId)
                .creatorId(Long.parseLong(fields.get(CacheFieldConstants.CREATOR_ID).toString()))
                .title(getStr(fields, CacheFieldConstants.TITLE))
                .type(getInt(fields, CacheFieldConstants.TYPE))
                .content(getStr(fields, CacheFieldConstants.CONTENT))
                .extraMediaUrl(getStr(fields, CacheFieldConstants.EXTRA_MEDIA_URL))
                .likeCount(getLong(fields, CacheFieldConstants.LIKE_COUNT))
                .replyCount(getLong(fields, CacheFieldConstants.REPLY_COUNT))
                .collectCount(getLong(fields, CacheFieldConstants.COLLECT_COUNT))
                .status(getInt(fields, CacheFieldConstants.STATUS))
                .createTime(getTimestamp(fields, CacheFieldConstants.CREATE_TIME))
                .updateTime(getTimestamp(fields, CacheFieldConstants.UPDATE_TIME))
                .isTop(getInt(fields, CacheFieldConstants.IS_TOP))
                .viewCount(getLong(fields, CacheFieldConstants.VIEW_COUNT))
                .visibleScope(getInt(fields, CacheFieldConstants.VISIBLE_SCOPE))
                .build();
    }

    private long randomExpire() {
        return BASE_EXPIRE_SECONDS + ThreadLocalRandom.current().nextInt(RANDOM_OFFSET_BOUND);
    }

    private String getStr(Map<Object, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private Integer getInt(Map<Object, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? Integer.valueOf(val.toString()) : null;
    }

    private Long getLong(Map<Object, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? Long.valueOf(val.toString()) : null;
    }

    private Timestamp getTimestamp(Map<Object, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? new Timestamp(Long.parseLong(val.toString())) : null;
    }
}

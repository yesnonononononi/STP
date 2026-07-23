package com.summit.stp.comment.application.service.impl;

import com.summit.stp.comment.application.service.CommentCacheProvider;
import com.summit.stp.comment.application.vo.CommentVO;
import com.summit.stp.comment.domain.repository.CommentRepository;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.comment.infrastructure.constants.CommentConstants;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class CommentCacheProviderImpl implements CommentCacheProvider {
    private final RedisTemplate redisTemplate;
    private final CommentRepository commentRepository;

    @Override
    public Map<Long, Map<String, Long>> getLikeCountAndReplyCount(List<Long> list) {
        HashMap<Long, Map<String, Long>> resultMap = new HashMap<>(list.size());
        if (list.isEmpty()) return resultMap;

        // Tip: 确保所有查询评论的缓存已被预热（包含缺失的点赞 Set 和回复数 String）
        loadCache(list);

        // Tip: 批量 Pipeline 从 Redis 分别获取点赞数 (SCARD) 和回复数 (GET)
        List<Object> resList = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) throws DataAccessException {
                SetOperations setOps = operations.opsForSet();
                ValueOperations valOps = operations.opsForValue();
                for (Long id : list) {
                    resultMap.put(id, new HashMap<>(2));
                    // Tip: 获取点赞 Set 集合中的元素个数
                    setOps.size(buildKey(CommentConstants.Cache.LIKE, id));
                    // Tip: 获取回复数 String 缓存中的值
                    valOps.get(buildKey(CommentConstants.Cache.REPLY_COUNT, id));
                }
                return null;
            }
        });

        // Tip: 遍历并解析 Pipeline 批量获取的缓存计数值
        for (int i = 0; i < list.size(); i++) {
            Long commentId = list.get(i);
            Map<String, Long> commentMap = resultMap.get(commentId);
            
            // Tip: 校验并扣除点赞 Set 里的防穿透占位符 -1L
            Long rawLikeCount = (Long) resList.get(i * 2);
            String likeKey = buildKey(CommentConstants.Cache.LIKE, commentId);
            Boolean hasDummy = redisTemplate.opsForSet().isMember(likeKey, -1L);
            long realLikeCount = rawLikeCount != null ? (hasDummy ? Math.max(0, rawLikeCount - 1) : rawLikeCount) : 0L;
            commentMap.put(CommentCacheProvider.LIKE_COUNT, realLikeCount);

            // Tip: 解析从 String 结构中读取的回复数值
            Object replyVal = resList.get(i * 2 + 1);
            long realReplyCount = 0L;
            if (replyVal instanceof Number num) {
                realReplyCount = num.longValue();
            } else if (replyVal instanceof String str) {
                try {
                    realReplyCount = Long.parseLong(str);
                } catch (NumberFormatException ignored) {}
            }
            commentMap.put(CommentCacheProvider.REPLY_COUNT, realReplyCount);
        }

        return resultMap;
    }

    @Override
    public Map<Long, Map<String, Boolean>> getIsLikeAndReplyByIds(List<CommentVO> list) {
        List<Long> ids = list.stream().map(CommentVO::getId).toList();
        HashMap<Long, Map<String, Boolean>> resultMap = new HashMap<>(list.size());
        if (ids.isEmpty()) return resultMap;

        // Tip: 获取当前登录人 ID
        Long currentUserId = UserHolder.getUser().getId();
         if(currentUserId == null){
             return list.stream().collect(Collectors.toMap(CommentVO::getId, comment -> Map.of(comment.getId().toString(), false)));
         }
        // Tip: 批量 Pipeline 检测用户是否点赞/作者是否赞过/作者是否回复过
        List<Object> result = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) throws DataAccessException {
                SetOperations setOps = operations.opsForSet();
                for (int i = 0; i < ids.size(); i++) {
                    Long commentId = ids.get(i);
                    Long publisherId = list.get(i).getPostPublisherId();

                    resultMap.put(commentId, new HashMap<>(3));
                    String userIsLikeKey = buildKey(CommentConstants.Cache.LIKE, commentId);
                    String isReplyKey =  buildKey(CommentConstants.Cache.REPLY_COUNT, commentId);

                    // Tip: 检测作者本人是否点赞过该评论
                    setOps.isMember(userIsLikeKey, publisherId);
                    // Tip: 检测当前操作用户是否点赞过该评论
                    setOps.isMember(userIsLikeKey, currentUserId);
                    // Tip: 检查该评论的回复数是否大于 0 (这里通过回复数 String 计数值判断)
                    operations.opsForValue().get(isReplyKey);
                }
                return null;
            }
        });

        // Tip: 将 Pipeline 批量检测的结果装配成响应的 Boolean Map
        for (int i = 0; i < ids.size(); i++) {
            Long commentId = ids.get(i);
            Map<String, Boolean> commentMap = resultMap.get(commentId);
            
            // Tip: 解析作者和当前用户是否已点赞的 Boolean 状态
            commentMap.put(CommentCacheProvider.CREATOR_IS_LIKE, Boolean.TRUE.equals(result.get(i * 3)));
            commentMap.put(CommentCacheProvider.IS_LIKE, Boolean.TRUE.equals(result.get(i * 3 + 1)));

            // Tip: 校验回复数是否非空且大于 0 以判断作者/他人是否回复过
            Object replyVal = result.get(i * 3 + 2);
            long replyCount = 0L;
            if (replyVal instanceof Number num) {
                replyCount = num.longValue();
            } else if (replyVal instanceof String str) {
                try {
                    replyCount = Long.parseLong(str);
                } catch (NumberFormatException ignored) {}
            }
            commentMap.put(CommentCacheProvider.CREATOR_IS_REPLIED, replyCount > 0);
        }

        return resultMap;
    }

    @Override
    public void init(Long commentId, Long replyCount, Long likeCount) {
        if (commentId == null) return;
        
        // Tip: 批量 Pipeline 初始化评论的点赞 Set 集合与回复数 String
        redisTemplate.executePipelined(new SessionCallback<>(){
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // Tip: 点赞 Set 集合初始化，放入 -1L 作为防穿透占位符
                String likeKey = buildKey(CommentConstants.Cache.LIKE, commentId);
                operations.opsForSet().add(likeKey, -1L);
                redisTemplate.expire(likeKey, CommentConstants.Cache.EXPIRE_TIME, TimeUnit.DAYS);

                // Tip: 回复数 String 初始化为传入的计数值
                String replyKey = buildKey(CommentConstants.Cache.REPLY_COUNT, commentId);
                operations.opsForValue().set(replyKey, replyCount != null ? replyCount : 0L);
                redisTemplate.expire(replyKey, CommentConstants.Cache.EXPIRE_TIME, TimeUnit.DAYS);
                return null;
            }
        });
    }

    @Override
    public boolean like(Long commentId) {
        if (commentId == null) return false;
        
        // Tip: 强制预热点赞与回复数缓存，防止误删或计数错误
        loadCache(commentId);
        
        boolean isOK;
        String key = buildKey(CommentConstants.Cache.LIKE, commentId);
        Long userId = UserHolder.getUser().getId();
        
        // Tip: 尝试往 Set 集合中添加该用户 ID
        Long added = redisTemplate.opsForSet().add(key, userId);
        if (added != null && added > 0) {
            // Tip: 添加成功表示执行“点赞”操作
            isOK = true;
        } else {
            // Tip: 成员已存在说明是重复点击，执行“取消点赞”并移出 Set
            redisTemplate.opsForSet().remove(key, userId);
            isOK = false;
        }
        
        // Tip: 标记本条评论缓存发生了更新，便于定时落库任务扫描
        markCacheChanged(commentId);
        return isOK;
    }

    @Override
    public void reply(Long commentId) {
        if (commentId == null) return;
        
        // Tip: 强制预热回复数 String 缓存，防穿透
        loadCache(commentId);
        
        // Tip: 将回复数 String 的值执行原子自增 +1
        String key = buildKey(CommentConstants.Cache.REPLY_COUNT, commentId);
        redisTemplate.opsForValue().increment(key);
        
        // Tip: 标记评论回复数有变动，以便定时任务同步至数据库
        markReplyChanged(commentId);
    }

    @Override
    public void markCacheChanged(Long commentId) {
        // Tip: 将发生点赞改变的评论 ID 加入全局更新 Set
        redisTemplate.opsForSet().add(CommentConstants.Cache.CHANGED, commentId);
    }

    @Override
    public void markReplyChanged(Long commentId) {
        // Tip: 将发生回复数改变的评论 ID 加入全局回复更新 Set
        if (commentId != null) {
            redisTemplate.opsForSet().add(CommentConstants.Cache.REPLY_CHANGED, commentId);
        }
    }

    @Override
    public void loadCache(Long commentId) {
        if (commentId == null) return;
        loadCache(List.of(commentId));
    }

    @Override
    public void loadCache(List<Long> list) {
        if (list == null || list.isEmpty()) return;

        // Tip: 批量 Pipeline 检测哪些 key 在 Redis 中已失效
        List<Object> existsResults = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) throws DataAccessException {
                for (Long id : list) {
                    operations.hasKey(buildKey(CommentConstants.Cache.LIKE, id));
                    operations.hasKey(buildKey(CommentConstants.Cache.REPLY_COUNT, id));
                }
                return null;
            }
        });

        List<Long> missingLikeIds = new ArrayList<>();
        List<Long> missingReplyIds = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            Long commentId = list.get(i);
            Object likeExists = existsResults.get(i * 2);
            Object replyExists = existsResults.get(i * 2 + 1);

            // Tip: 区分哪些评论缺失点赞缓存或回复数缓存
            if (likeExists == null || Boolean.FALSE.equals(likeExists)) {
                missingLikeIds.add(commentId);
            }
            if (replyExists == null || Boolean.FALSE.equals(replyExists)) {
                missingReplyIds.add(commentId);
            }
        }

        // Tip: 拒绝在循环中查数据库。一次性批量查出缺失评论在数据库中的回复计数
        Map<Long, Long> dbReplyCounts = missingReplyIds.isEmpty() ? new HashMap<>() : commentRepository.queryReplyCounts(missingReplyIds);

        // Tip: 批量将失效缓存回源并写入 Redis 缓存（Set 和 String）
        if (!missingLikeIds.isEmpty() || !missingReplyIds.isEmpty()) {
            redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public Object execute(@NonNull RedisOperations operations) throws DataAccessException {
                    SetOperations setOps = operations.opsForSet();
                    ValueOperations valOps = operations.opsForValue();

                    // Tip: 回源预热点赞 Set 缓存关系
                    for (Long id : missingLikeIds) {
                        String key = buildKey(CommentConstants.Cache.LIKE, id);
                        // Tip: 写入 -1L 防缓存击穿占位符
                        setOps.add(key, -1L);
                        
                        // Tip: 批量拉取数据库中的真实点赞人列表并写入 Set
                        Set<Long> likeUserIds = commentRepository.getLikeUserIds(id);
                        if (likeUserIds != null && !likeUserIds.isEmpty()) {
                            for (Long userId : likeUserIds) {
                                setOps.add(key, userId);
                            }
                        }
                        operations.expire(key, CommentConstants.Cache.EXPIRE_TIME, TimeUnit.DAYS);
                    }

                    // Tip: 回源预热回复数 String 计数值
                    for (Long id : missingReplyIds) {
                        String key = buildKey(CommentConstants.Cache.REPLY_COUNT, id);
                        Long count = dbReplyCounts.getOrDefault(id, 0L);
                        valOps.set(key, count);
                        operations.expire(key, CommentConstants.Cache.EXPIRE_TIME, TimeUnit.DAYS);
                    }
                    return null;
                }
            });
        }
    }

    @Override
    public Set<Long> getLikeUserIds(Long commentId) {
        if (commentId == null) return Collections.emptySet();
        
        // Tip: 从 Redis 的 Set 集合中获取点赞的所有用户 ID
        String key = buildKey(CommentConstants.Cache.LIKE, commentId);
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members == null) return Collections.emptySet();
        
        Set<Long> userIds = new HashSet<>();
        for (Object member : members) {
            // Tip: 过滤掉占位符 -1L
            if (member instanceof Long val && !val.equals(-1L)) {
                userIds.add(val);
            } else if (member instanceof Integer val && !val.equals(-1)) {
                userIds.add(val.longValue());
            }
        }
        return userIds;
    }

    private String buildKey(String prefix, Long commentId) {
        // Tip: 组装特定的 Redis 键名前缀与 ID 标识
        return prefix + commentId;
    }
}

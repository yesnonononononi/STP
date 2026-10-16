package com.summit.stp.post.application.service.impl.cache;

import com.summit.stp.post.infrastructure.constants.PostConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.lang.NonNull;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 帖子互动缓存操作（点赞/收藏统一模板），消除原有的对称重复代码
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class PostInteractionCacheOps {

    /** 互动切换 Lua 脚本资源路径，供 Spring 装配与测试直连两种场景复用。 */
    private static final String TOGGLE_SCRIPT_LOCATION = "file/post_interaction_toggle.lua";

    private final RedisTemplate<String, Object> redisTemplate;

    private final DefaultRedisScript<Long> toggleScript;

    /**
     * 测试/直连场景构造方法：自行从 classpath 加载 Lua 脚本。
     *
     * @param redisTemplate 帖子模块 Redis 模板
     */
    public PostInteractionCacheOps(RedisTemplate<String, Object> redisTemplate) {
        this(redisTemplate, loadToggleScript());
    }

    /**
     * Spring 容器构造方法：注入容器管理的脚本 Bean。
     *
     * @param redisTemplate 帖子模块 Redis 模板
     * @param toggleScript  由 {@code PostRedisScriptConfiguration} 注册的切换脚本
     */
    @Autowired
    public PostInteractionCacheOps(RedisTemplate<String, Object> redisTemplate,
                                   @Qualifier("postInteractionToggleScript") DefaultRedisScript<Long> toggleScript) {
        this.redisTemplate = redisTemplate;
        this.toggleScript = toggleScript;
    }

    /**
     * 从 classpath 加载互动切换脚本（测试直连时使用）。
     *
     * @return 结果类型为 Long 的脚本
     */
    private static DefaultRedisScript<Long> loadToggleScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(TOGGLE_SCRIPT_LOCATION)));
        script.setResultType(Long.class);
        return script;
    }

    /**
     * 切换互动状态（点赞/收藏 toggle）
     * 0 : 未互动
     * 1 : 已互动
     *
     * <p>通过 Lua 脚本把"读判定 + 改写集合与计数"合并为单次原子操作，
     * 保证互动计数恒等于互动集合真实规模，避免并发 toggle 时的计数漂移。</p>
     *
     * @param postId 帖子 ID
     * @param userId 用户 ID
     * @param type   互动类型（点赞/收藏）
     * @return 互动状态，标识是否是第一次互动
     */
    public boolean toggle(Long postId, Long userId, InteractionType type) {
        String setKey = type.buildSetKey(postId);
        String postKey = PostConstants.Cache.DETAIL_PREFIX + postId;
        try {
            // KEYS 走 key 序列化器（原始字符串），ARGV 走 value 序列化器（与 opsForZSet 写入的 member 字节一致），
            // 计数 field 名通过 KEYS[3] 传递以避免被 Jackson 二次序列化。
            Long result = redisTemplate.execute(
                    toggleScript,
                    Arrays.asList(setKey, postKey, type.getCountField()),
                    userId
            );
            return result != null && result == 1L;
        } catch (Exception e) {
            log.warn("【帖子模块】互动写入异常，type={}，动作：原子切换", type, e);
            throw e;
        }
    }

    /**
     * 判断用户是否已互动
     */
    public boolean isMember(Long postId, Long userId, InteractionType type) {
        if (userId == null || userId.equals(-1L)) {
            return false;
        }
        try {
            Double score = redisTemplate.opsForZSet().score(type.buildSetKey(postId), userId);
            return score != null && score == 1.0;
        } catch (Exception e) {
            log.warn("【帖子模块】判断互动状态异常，type={}，动作：isMember", type, e);
            return false;
        }
    }

    /**
     * 批量获取互动用户 ID 集合
     */
    public Map<Long, Set<Long>> batchGetUserIds(List<Long> postIdList, InteractionType type) {
        List<Object> list = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) throws DataAccessException {
                for (Long postId : postIdList) {
                    operations.opsForZSet().rangeByScore(type.buildSetKey(postId), 1.0, 1.0);
                }
                return null;
            }
        });

        Map<Long, Set<Long>> result = new HashMap<>(postIdList.size());
        for (int i = 0; i < postIdList.size(); i++) {
            Long postId = postIdList.get(i);
            Object raw = list.get(i);
            if (raw instanceof Collection<?> s && !s.isEmpty()) {
                Set<Long> filtered = new HashSet<>();
                for (Object member : s) {
                    if(member == null)continue;
                    Long uid = Long.valueOf(member.toString());
                    if (!uid.equals(-1L)) {
                        filtered.add(uid);
                    }
                }
                result.put(postId, filtered);
            }
        }
        return result;
    }

    /**
     * 批量判断用户是否已互动
     */
    public Map<Long, Boolean> batchIsMember(List<Long> postIdList, Long userId, InteractionType type) {
        if (userId == null || userId.equals(-1L)) {
            Map<Long, Boolean> map = new HashMap<>(postIdList.size());
            for (Long postId : postIdList) {
                map.put(postId, false);
            }
            return map;
        }
        List<Object> results = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) {
                for (Long postId : postIdList) {
                    operations.opsForZSet().score(type.buildSetKey(postId), userId);
                }
                return null;
            }
        });

        Map<Long, Boolean> map = new HashMap<>(postIdList.size());
        for (int i = 0; i < postIdList.size(); i++) {
            Object raw = results.get(i);
            boolean isMember = false;
            if (raw instanceof Double score) {
                isMember = (score == 1.0);
            }
            map.put(postIdList.get(i), isMember);
        }
        return map;
    }

    /**
     * 加载单个帖子的互动 ZSet 缓存（从 DB 预热）
     */
    public void loadInteractionSet(Long postId, InteractionType type, List<Long> userIdsFromDb) {
        String setKey = type.buildSetKey(postId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(setKey))) {
            return;
        }
        redisTemplate.opsForZSet().add(setKey, -1L, 1.0);
        if (userIdsFromDb != null && !userIdsFromDb.isEmpty()) {
            for (Long uid : userIdsFromDb) {
                redisTemplate.opsForZSet().add(setKey, uid, 1.0);
            }
        }
        redisTemplate.expire(setKey, 7, TimeUnit.DAYS);
    }

    /**
     * 批量加载缺失的互动 ZSet 缓存（Pipeline 批量预热）
     */
    public void batchLoadMissingSets(
            List<Long> missingPostIds,
            InteractionType type,
            Function<List<Long>, Map<Long, List<Long>>> dbBatchLoader
    ) {
        if (missingPostIds.isEmpty()) {
            return;
        }
        Map<Long, List<Long>> userMap = dbBatchLoader.apply(missingPostIds);
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) {
                for (Long pid : missingPostIds) {
                    String setKey = type.buildSetKey(pid);
                    operations.opsForZSet().add(setKey, -1L, 1.0);
                    List<Long> users = userMap.getOrDefault(pid, Collections.emptyList());
                    for (Long uid : users) {
                        operations.opsForZSet().add(setKey, uid, 1.0);
                    }
                    operations.expire(setKey, 7, TimeUnit.DAYS);
                }
                return null;
            }
        });
    }

    /**
     * 标记互动已变更（加入 changed set）
     */
    public void markChanged(Object postId, String changedSetKey) {
        redisTemplate.opsForSet().add(changedSetKey, postId);
    }
}

package com.summit.stp.post.application.service.impl.cache;

import com.summit.stp.post.infrastructure.constants.PostConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link PostInteractionCacheOps} 的纯单元测试（Mockito，不依赖 Redis）。
 *
 * <p>重点覆盖本次并发修复的 {@code toggle}（Lua 原子切换）的传参与返回值语义，
 * 并顺带覆盖 {@code isMember} / {@code markChanged} / {@code loadInteractionSet}，
 * 为 PIT 变异测试提供有杀伤力的断言。</p>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
class PostInteractionCacheOpsTest {

    private static final long POST_ID = 10L;

    private static final long USER_ID = 7L;

    private RedisTemplate<String, Object> redisTemplate;

    private ZSetOperations<String, Object> zSetOps;

    private SetOperations<String, Object> setOps;

    private PostInteractionCacheOps ops;

    private String likeKey;

    private String postKey;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        zSetOps = mock(ZSetOperations.class);
        setOps = mock(SetOperations.class);
        ops = new PostInteractionCacheOps(redisTemplate);
        likeKey = InteractionType.LIKE.buildSetKey(POST_ID);
        postKey = PostConstants.Cache.DETAIL_PREFIX + POST_ID;
    }

    // ==================== toggle ====================

    @Test
    @DisplayName("toggle: 脚本返回 1 -> true（首次互动）")
    void toggle_scriptReturnsOne_true() {
        doReturn(1L).when(redisTemplate).execute(any(RedisScript.class), anyList(), any());

        assertThat(ops.toggle(POST_ID, USER_ID, InteractionType.LIKE)).isTrue();
    }

    @Test
    @DisplayName("toggle: 脚本返回 0 -> false")
    void toggle_scriptReturnsZero_false() {
        doReturn(0L).when(redisTemplate).execute(any(RedisScript.class), anyList(), any());

        assertThat(ops.toggle(POST_ID, USER_ID, InteractionType.LIKE)).isFalse();
    }

    @Test
    @DisplayName("toggle: 脚本返回 null -> false")
    void toggle_scriptReturnsNull_false() {
        doReturn(null).when(redisTemplate).execute(any(RedisScript.class), anyList(), any());

        assertThat(ops.toggle(POST_ID, USER_ID, InteractionType.LIKE)).isFalse();
    }

    @Test
    @DisplayName("toggle: 校验 execute 的 key 与 arg 传参（点赞）")
    void toggle_like_passesExpectedKeysAndArg() {
        doReturn(1L).when(redisTemplate).execute(any(RedisScript.class), anyList(), any());

        ops.toggle(POST_ID, USER_ID, InteractionType.LIKE);

        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass((Class) List.class);
        ArgumentCaptor<Object> argCaptor = ArgumentCaptor.forClass(Object.class);
        verify(redisTemplate).execute(any(RedisScript.class), keysCaptor.capture(), argCaptor.capture());
        assertThat(keysCaptor.getValue()).containsExactly(likeKey, postKey, InteractionType.LIKE.getCountField());
        assertThat(argCaptor.getValue()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("toggle: 校验 execute 的 key 与 arg 传参（收藏）")
    void toggle_collect_passesExpectedKeysAndArg() {
        doReturn(0L).when(redisTemplate).execute(any(RedisScript.class), anyList(), any());

        ops.toggle(POST_ID, USER_ID, InteractionType.COLLECT);

        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass((Class) List.class);
        ArgumentCaptor<Object> argCaptor = ArgumentCaptor.forClass(Object.class);
        verify(redisTemplate).execute(any(RedisScript.class), keysCaptor.capture(), argCaptor.capture());
        assertThat(keysCaptor.getValue()).containsExactly(
                InteractionType.COLLECT.buildSetKey(POST_ID), postKey, InteractionType.COLLECT.getCountField());
        assertThat(argCaptor.getValue()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("toggle: 脚本执行异常 -> 向上抛出")
    void toggle_scriptError_propagates() {
        doThrow(new RuntimeException("redis error"))
                .when(redisTemplate).execute(any(RedisScript.class), anyList(), any());

        assertThatThrownBy(() -> ops.toggle(POST_ID, USER_ID, InteractionType.LIKE))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("redis error");
    }

    // ==================== isMember ====================

    @Test
    @DisplayName("isMember: userId 为 null -> false 且不访问 Redis")
    void isMember_nullUser_false() {
        assertThat(ops.isMember(POST_ID, null, InteractionType.LIKE)).isFalse();
        verifyNoInteractions(redisTemplate);
    }

    @Test
    @DisplayName("isMember: userId 为 -1 -> false")
    void isMember_dummyUser_false() {
        assertThat(ops.isMember(POST_ID, -1L, InteractionType.LIKE)).isFalse();
        verifyNoInteractions(redisTemplate);
    }

    @Test
    @DisplayName("isMember: score=1.0 -> true")
    void isMember_scoreOne_true() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.score(likeKey, USER_ID)).thenReturn(1.0);

        assertThat(ops.isMember(POST_ID, USER_ID, InteractionType.LIKE)).isTrue();
    }

    @Test
    @DisplayName("isMember: score 为 null -> false")
    void isMember_scoreNull_false() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.score(likeKey, USER_ID)).thenReturn(null);

        assertThat(ops.isMember(POST_ID, USER_ID, InteractionType.LIKE)).isFalse();
    }

    @Test
    @DisplayName("isMember: 异常 -> 降级 false")
    void isMember_exception_false() {
        when(redisTemplate.opsForZSet()).thenThrow(new RuntimeException("boom"));

        assertThat(ops.isMember(POST_ID, USER_ID, InteractionType.LIKE)).isFalse();
    }

    // ==================== markChanged ====================

    @Test
    @DisplayName("markChanged: 加入变更集合")
    void markChanged_addsToSet() {
        when(redisTemplate.opsForSet()).thenReturn(setOps);

        ops.markChanged(POST_ID, PostConstants.Cache.CHANGED);

        verify(setOps).add(PostConstants.Cache.CHANGED, POST_ID);
    }

    // ==================== loadInteractionSet ====================

    @Test
    @DisplayName("loadInteractionSet: key 已存在 -> 不做任何写入")
    void loadInteractionSet_exists_skip() {
        when(redisTemplate.hasKey(likeKey)).thenReturn(Boolean.TRUE);

        ops.loadInteractionSet(POST_ID, InteractionType.LIKE, List.of(1L, 2L));

        verify(redisTemplate, never()).opsForZSet();
        verify(redisTemplate, never()).expire(any(), any(Long.class), any(TimeUnit.class));
    }

    @Test
    @DisplayName("loadInteractionSet: key 不存在 -> 写入占位符与用户并设置过期")
    void loadInteractionSet_missing_writes() {
        when(redisTemplate.hasKey(likeKey)).thenReturn(Boolean.FALSE);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

        ops.loadInteractionSet(POST_ID, InteractionType.LIKE, List.of(1L, 2L));

        verify(zSetOps).add(likeKey, -1L, 1.0);
        verify(zSetOps).add(likeKey, 1L, 1.0);
        verify(zSetOps).add(likeKey, 2L, 1.0);
        verify(redisTemplate).expire(likeKey, 7, TimeUnit.DAYS);
    }
}

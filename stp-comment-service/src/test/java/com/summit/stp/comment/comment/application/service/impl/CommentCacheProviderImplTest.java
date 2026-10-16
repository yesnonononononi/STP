package com.summit.stp.comment.comment.application.service.impl;

import com.summit.stp.comment.comment.application.service.CommentCacheProvider;
import com.summit.stp.comment.comment.application.vo.CommentVO;
import com.summit.stp.comment.comment.domain.repository.CommentRepository;
import com.summit.stp.comment.comment.infrastructure.constants.CommentConstants;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.mockito.stubbing.OngoingStubbing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link CommentCacheProviderImpl} 的纯单元测试（Mockito，不依赖 Redis）。
 *
 * <p>覆盖点赞切换、回复自增、变更标记、批量计数读取等分支，
 * 为 PIT 变异测试提供有杀伤力的断言。</p>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
class CommentCacheProviderImplTest {

    private static final long USER_ID = 7L;

    private static final long COMMENT_ID = 1L;

    private RedisTemplate redisTemplate;

    private CommentRepository commentRepository;

    private SetOperations setOps;

    private ValueOperations valueOps;

    private RedisOperations redisOperations;

    private CommentCacheProviderImpl provider;

    private String likeKey;

    private String replyKey;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        commentRepository = mock(CommentRepository.class);
        setOps = mock(SetOperations.class);
        valueOps = mock(ValueOperations.class);
        redisOperations = mock(RedisOperations.class);
        provider = new CommentCacheProviderImpl(redisTemplate, commentRepository);
        likeKey = CommentConstants.Cache.LIKE + COMMENT_ID;
        replyKey = CommentConstants.Cache.REPLY_COUNT + COMMENT_ID;
        UserHolder.setUser(UserSession.builder().id(USER_ID).build());
        when(redisOperations.opsForSet()).thenReturn(setOps);
        when(redisOperations.opsForValue()).thenReturn(valueOps);
    }

    @AfterEach
    void tearDown() {
        UserHolder.clear();
    }

    // ==================== like ====================

    @Test
    @DisplayName("like: 首次点赞（add 返回 1）-> true 并标记变更")
    void like_firstTime_true() {
        when(redisTemplate.executePipelined(any(SessionCallback.class)))
                .thenReturn(Arrays.<Object>asList(Boolean.TRUE, Boolean.TRUE));
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.add(likeKey, USER_ID)).thenReturn(1L);

        assertThat(provider.like(COMMENT_ID)).isTrue();
        verify(setOps).add(CommentConstants.Cache.CHANGED, COMMENT_ID);
    }

    @Test
    @DisplayName("like: 重复点赞（add 返回 0）-> 取消点赞并返回 false")
    void like_duplicate_cancel() {
        when(redisTemplate.executePipelined(any(SessionCallback.class)))
                .thenReturn(Arrays.<Object>asList(Boolean.TRUE, Boolean.TRUE));
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.add(likeKey, USER_ID)).thenReturn(0L);

        assertThat(provider.like(COMMENT_ID)).isFalse();
        verify(setOps).remove(likeKey, USER_ID);
        verify(setOps).add(CommentConstants.Cache.CHANGED, COMMENT_ID);
    }

    @Test
    @DisplayName("like: commentId 为 null -> false 且不访问 Redis")
    void like_nullId_false() {
        assertThat(provider.like(null)).isFalse();
        verify(redisTemplate, never()).executePipelined(any(SessionCallback.class));
    }

    // ==================== reply ====================

    @Test
    @DisplayName("reply: 自增回复数并标记变更")
    void reply_increments() {
        when(redisTemplate.executePipelined(any(SessionCallback.class)))
                .thenReturn(Arrays.<Object>asList(Boolean.TRUE, Boolean.TRUE));
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        provider.reply(COMMENT_ID);

        verify(valueOps).increment(replyKey);
        verify(setOps).add(CommentConstants.Cache.REPLY_CHANGED, COMMENT_ID);
    }

    @Test
    @DisplayName("reply: commentId 为 null -> 不访问 Redis")
    void reply_nullId_noop() {
        provider.reply(null);
        verify(redisTemplate, never()).opsForValue();
    }

    // ==================== markCacheChanged / markReplyChanged ====================

    @Test
    @DisplayName("markCacheChanged: 加入全局更新集合")
    void markCacheChanged_adds() {
        when(redisTemplate.opsForSet()).thenReturn(setOps);

        provider.markCacheChanged(COMMENT_ID);

        verify(setOps).add(CommentConstants.Cache.CHANGED, COMMENT_ID);
    }

    @Test
    @DisplayName("markReplyChanged: 非空加入回复更新集合")
    void markReplyChanged_adds() {
        when(redisTemplate.opsForSet()).thenReturn(setOps);

        provider.markReplyChanged(COMMENT_ID);

        verify(setOps).add(CommentConstants.Cache.REPLY_CHANGED, COMMENT_ID);
    }

    @Test
    @DisplayName("markReplyChanged: null -> 不写入")
    void markReplyChanged_null_noop() {
        provider.markReplyChanged(null);
        verify(redisTemplate, never()).opsForSet();
    }

    // ==================== getLikeUserIds ====================

    @Test
    @DisplayName("getLikeUserIds: 过滤占位符 -1，兼容 Long/Integer")
    void getLikeUserIds_filtersDummy() {
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        Set<Object> members = new HashSet<>(Arrays.asList(1L, -1L, 2));
        when(setOps.members(likeKey)).thenReturn(members);

        Set<Long> ids = provider.getLikeUserIds(COMMENT_ID);

        assertThat(ids).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("getLikeUserIds: commentId 为 null -> 空集")
    void getLikeUserIds_nullId_empty() {
        assertThat(provider.getLikeUserIds(null)).isEmpty();
    }

    @Test
    @DisplayName("getLikeUserIds: members 为 null -> 空集")
    void getLikeUserIds_nullMembers_empty() {
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.members(likeKey)).thenReturn(null);

        assertThat(provider.getLikeUserIds(COMMENT_ID)).isEmpty();
    }

    // ==================== getLikeCountAndReplyCount ====================

    @Test
    @DisplayName("getLikeCountAndReplyCount: 空列表 -> 空 Map")
    void getLikeCountAndReplyCount_empty() {
        assertThat(provider.getLikeCountAndReplyCount(new ArrayList<>())).isEmpty();
    }

    @Test
    @DisplayName("getLikeCountAndReplyCount: 解析 SCARD 与回复数（无占位符）")
    void getLikeCountAndReplyCount_normal() {
        when(redisOperations.hasKey(any())).thenReturn(Boolean.TRUE);
        List<Object> exists = Arrays.<Object>asList(Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
        List<Object> counts = Arrays.<Object>asList(2L, "5", 3L, "0");
        stubPipelines(exists, counts);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.isMember(anyString(), eq(-1L))).thenReturn(Boolean.FALSE);

        Map<Long, Map<String, Long>> result = provider.getLikeCountAndReplyCount(List.of(1L, 2L));

        assertThat(result.get(1L))
                .containsEntry(CommentCacheProvider.LIKE_COUNT, 2L)
                .containsEntry(CommentCacheProvider.REPLY_COUNT, 5L);
        assertThat(result.get(2L))
                .containsEntry(CommentCacheProvider.LIKE_COUNT, 3L)
                .containsEntry(CommentCacheProvider.REPLY_COUNT, 0L);
    }

    @Test
    @DisplayName("getLikeCountAndReplyCount: 命中 -1 占位符 -> 点赞数扣减 1")
    void getLikeCountAndReplyCount_dummyPlaceholder() {
        when(redisOperations.hasKey(any())).thenReturn(Boolean.TRUE);
        stubPipelines(Arrays.<Object>asList(Boolean.TRUE, Boolean.TRUE), Arrays.<Object>asList(2L, 1L));
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.isMember(anyString(), eq(-1L))).thenReturn(Boolean.TRUE);

        Map<Long, Map<String, Long>> result = provider.getLikeCountAndReplyCount(List.of(1L));

        assertThat(result.get(1L)).containsEntry(CommentCacheProvider.LIKE_COUNT, 1L);
    }

    // ==================== getIsLikeAndReplyByIds ====================

    @Test
    @DisplayName("getIsLikeAndReplyByIds: 空列表 -> 空 Map")
    void getIsLikeAndReplyByIds_empty() {
        assertThat(provider.getIsLikeAndReplyByIds(new ArrayList<>())).isEmpty();
    }

    @Test
    @DisplayName("getIsLikeAndReplyByIds: 未登录 -> 全部 false")
    void getIsLikeAndReplyByIds_noLogin() {
        UserHolder.setUser(UserSession.builder().id(null).build());
        CommentVO vo = CommentVO.builder().id(COMMENT_ID).postPublisherId(9L).build();

        Map<Long, Map<String, Boolean>> result = provider.getIsLikeAndReplyByIds(List.of(vo));

        assertThat(result.get(COMMENT_ID)).containsEntry(Long.toString(COMMENT_ID), false);
    }

    @Test
    @DisplayName("getIsLikeAndReplyByIds: 正常解析作者/本人点赞与回复状态")
    void getIsLikeAndReplyByIds_normal() {
        stubPipelines(Arrays.<Object>asList(Boolean.TRUE, Boolean.FALSE, 3L));
        CommentVO vo = CommentVO.builder().id(COMMENT_ID).postPublisherId(9L).build();

        Map<Long, Map<String, Boolean>> result = provider.getIsLikeAndReplyByIds(List.of(vo));

        assertThat(result.get(COMMENT_ID))
                .containsEntry(CommentCacheProvider.CREATOR_IS_LIKE, true)
                .containsEntry(CommentCacheProvider.IS_LIKE, false)
                .containsEntry(CommentCacheProvider.CREATOR_IS_REPLIED, true);
    }

    // ==================== init / loadCache ====================

    @Test
    @DisplayName("init: commentId 为 null -> 不访问 Redis")
    void init_nullId_noop() {
        provider.init(null, 1L, 1L);
        verify(redisTemplate, never()).executePipelined(any(SessionCallback.class));
    }

    @Test
    @DisplayName("init: 初始化点赞集合与回复数")
    void init_normal() {
        when(redisTemplate.executePipelined(any(SessionCallback.class))).thenReturn(null);

        provider.init(COMMENT_ID, 5L, 3L);

        verify(redisTemplate).executePipelined(any(SessionCallback.class));
    }

    @Test
    @DisplayName("loadCache(List): null/空 -> 不访问 Redis")
    void loadCache_empty_noop() {
        provider.loadCache((List<Long>) null);
        provider.loadCache(new ArrayList<>());
        verify(redisTemplate, never()).executePipelined(any(SessionCallback.class));
    }

    @Test
    @DisplayName("loadCache(List): 缺失缓存 -> 回源数据库补齐")
    void loadCache_missing_prewarms() {
        when(redisTemplate.executePipelined(any(SessionCallback.class)))
                .thenReturn(Arrays.<Object>asList(Boolean.FALSE, Boolean.FALSE))
                .thenReturn(null);
        when(commentRepository.queryReplyCounts(List.of(COMMENT_ID))).thenReturn(Map.of(COMMENT_ID, 3L));

        provider.loadCache(List.of(COMMENT_ID));

        verify(commentRepository).queryReplyCounts(List.of(COMMENT_ID));
    }

    @Test
    @DisplayName("loadCache(Long): null -> 不访问 Redis")
    void loadCache_singleNull_noop() {
        provider.loadCache((Long) null);
        verify(redisTemplate, never()).executePipelined(any(SessionCallback.class));
    }

    /**
     * 依序为 {@code executePipelined} 打桩：每次调用都会真正执行传入的 SessionCallback，
     * 并返回对应的预置结果，从而覆盖 Pipeline 回调内的分支逻辑。
     *
     * @param results 按调用顺序预置的返回值
     */
    @SafeVarargs
    private final void stubPipelines(List<Object>... results) {
        OngoingStubbing<List<Object>> stubbing =
                when(redisTemplate.executePipelined(any(SessionCallback.class)));
        for (List<Object> result : results) {
            stubbing = stubbing.thenAnswer(invocation -> {
                SessionCallback callback = invocation.getArgument(0);
                callback.execute(redisOperations);
                return result;
            });
        }
    }
}

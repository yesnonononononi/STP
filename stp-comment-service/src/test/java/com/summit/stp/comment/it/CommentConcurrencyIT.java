package com.summit.stp.comment.it;

import com.summit.stp.comment.comment.application.service.CommentCacheProvider;
import com.summit.stp.comment.comment.application.service.impl.CommentCacheProviderImpl;
import com.summit.stp.comment.comment.domain.repository.CommentRepository;
import com.summit.stp.comment.comment.infrastructure.constants.CommentConstants;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 评论模块并发集成测试（真实 Redis）。
 *
 * <p>覆盖的并发不变量：</p>
 * <ul>
 *     <li>并发点赞：N 个不同用户点赞后点赞集合与点赞数一致；</li>
 *     <li>并发点赞/取消：集合成员数与计数始终一致；</li>
 *     <li>同一用户并发点赞/取消后集合规模不超过 1；</li>
 *     <li>并发回复：回复数不丢不重，等于并发次数；</li>
 *     <li>定时刷盘前置条件：点赞 / 回复变更标记集合被正确写入。</li>
 * </ul>
 *
 * <p>数据库仓储以 Mockito 隔离，仅验证 Redis 并发语义；定时刷盘由
 * {@code CommentLikeStateScheduler / CommentReplyCountScheduler} 消费变更集合，
 * 本测试校验其可被正确感知。</p>
 */
@Tag("integration")
class CommentConcurrencyIT extends AbstractCommentRedisIntegrationTest {

    private static final long COMMENT_ID = 8001L;

    private final CommentRepository commentRepository = Mockito.mock(CommentRepository.class);

    private final CommentCacheProviderImpl provider =
            new CommentCacheProviderImpl(REDIS_TEMPLATE, commentRepository);

    @BeforeEach
    void setUp() {
        flushCommentKeys();
        provider.init(COMMENT_ID, 0L, 0L);
    }

    @AfterEach
    void tearDown() {
        flushCommentKeys();
    }

    @Test
    @DisplayName("并发点赞：N 个不同用户点赞后点赞集合内容与计数一致")
    void concurrentLike_distinctUsers_setAndCountConsistent() throws Exception {
        int users = 50;
        Set<Long> expectedUserIds = new HashSet<>();
        for (int i = 0; i < users; i++) {
            expectedUserIds.add(900000L + i);
        }

        List<Throwable> errors = runConcurrently(users, i -> {
            bindUser(900000L + i);
            try {
                provider.like(COMMENT_ID);
            } finally {
                UserHolder.clear();
            }
        });

        assertThat(errors).isEmpty();
        Set<Long> actualUserIds = realLikeUserIds();
        assertThat(actualUserIds)
                .as("点赞集合应精确包含全部并发点赞用户（真实不变量，非 SCARD 同义反复）")
                .containsExactlyInAnyOrderElementsOf(expectedUserIds);
        assertThat(likeCount()).isEqualTo((long) users);
    }

    @Test
    @DisplayName("同用户并发切换点赞：集合规模不超过 1 且成员只能是该用户")
    void concurrentToggle_sameUser_consistent() throws Exception {
        long userId = 900999L;
        int threads = 30;

        List<Throwable> errors = runConcurrently(threads, i -> {
            bindUser(userId);
            try {
                provider.like(COMMENT_ID);
            } finally {
                UserHolder.clear();
            }
        });

        assertThat(errors).isEmpty();
        Set<Long> members = realLikeUserIds();
        assertThat(members).as("同一用户切换后点赞集合规模不应超过 1").hasSizeLessThanOrEqualTo(1);
        assertThat(members).as("集合中不应出现该用户以外的成员").allMatch(id -> id.equals(userId));
        assertThat(likeCount()).as("点赞数必须等于真实集合成员数").isEqualTo((long) members.size());
    }

    @Test
    @DisplayName("并发点赞后取消：N 个用户成对点赞/取消后集合与计数归零")
    void concurrentLikeThenUnlike_balanced() throws Exception {
        int users = 50;

        List<Throwable> errors = runConcurrently(users, i -> {
            long userId = 910000L + i;
            bindUser(userId);
            try {
                provider.like(COMMENT_ID);
                provider.like(COMMENT_ID);
            } finally {
                UserHolder.clear();
            }
        });

        assertThat(errors).isEmpty();
        assertThat(realLikeUserIds()).isEmpty();
        assertThat(likeCount()).isZero();
    }

    @Test
    @DisplayName("并发回复：回复数不丢不重，等于并发次数")
    void concurrentReply_noLostUpdate() throws Exception {
        int threads = 100;

        List<Throwable> errors = runConcurrently(threads, i -> provider.reply(COMMENT_ID));

        assertThat(errors).isEmpty();
        assertThat(replyCount()).isEqualTo((long) threads);
    }

    @Test
    @DisplayName("并发点赞+回复混合：两类计数互不干扰且各自准确")
    void concurrentLikeAndReply_consistent() throws Exception {
        int users = 40;
        int replies = 60;
        Set<Long> expectedUserIds = new HashSet<>();
        for (int i = 0; i < users; i++) {
            expectedUserIds.add(920000L + i);
        }

        List<Throwable> errors = runConcurrently(users, i -> {
            bindUser(920000L + i);
            try {
                provider.like(COMMENT_ID);
                provider.reply(COMMENT_ID);
            } finally {
                UserHolder.clear();
            }
        });
        List<Throwable> replyErrors = runConcurrently(replies, i -> provider.reply(COMMENT_ID));

        assertThat(errors).isEmpty();
        assertThat(replyErrors).isEmpty();
        assertThat(realLikeUserIds()).containsExactlyInAnyOrderElementsOf(expectedUserIds);
        assertThat(likeCount()).isEqualTo((long) users);
        assertThat(replyCount()).isEqualTo((long) (users + replies));
    }

    @Test
    @DisplayName("定时刷盘前置条件：变更标记集合会被写入，供调度器消费")
    void concurrentChanges_markedForScheduler() throws Exception {
        int users = 10;

        List<Throwable> errors = runConcurrently(users, i -> {
            bindUser(930000L + i);
            try {
                provider.like(COMMENT_ID);
            } finally {
                UserHolder.clear();
            }
        });
        assertThat(errors).isEmpty();

        assertThat(markerMembers(CommentConstants.Cache.CHANGED))
                .as("点赞变更集合应包含该评论 ID")
                .extracting(String::valueOf)
                .contains(String.valueOf(COMMENT_ID));
    }

    // ======================== 工具方法 ========================

    private List<Throwable> runConcurrently(int threads, IntConsumer task) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r, "comment-concurrency");
            t.setDaemon(true);
            return t;
        });
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threads; i++) {
            final int index = i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    task.accept(index);
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    done.countDown();
                }
            });
        }

        try {
            assertThat(ready.await(30, TimeUnit.SECONDS)).as("工作线程未能全部就绪").isTrue();
            start.countDown();
            assertThat(done.await(120, TimeUnit.SECONDS)).as("并发任务未在超时时间内完成").isTrue();
        } finally {
            pool.shutdownNow();
        }
        return errors;
    }

    private void bindUser(long userId) {
        UserHolder.setUser(UserSession.builder()
                .id(userId)
                .username("it-user-" + userId)
                .build());
    }

    private Set<Long> realLikeUserIds() {
        return provider.getLikeUserIds(COMMENT_ID);
    }

    private long likeCount() {
        return counterOf(CommentCacheProvider.LIKE_COUNT);
    }

    private long replyCount() {
        return counterOf(CommentCacheProvider.REPLY_COUNT);
    }

    private long counterOf(String field) {
        Map<String, Long> counts = provider.getLikeCountAndReplyCount(List.of(COMMENT_ID)).get(COMMENT_ID);
        return counts == null ? 0L : counts.getOrDefault(field, 0L);
    }

    private Set<Object> markerMembers(String key) {
        Set<Object> members = REDIS_TEMPLATE.opsForSet().members(key);
        return members == null ? Collections.emptySet() : members;
    }

    private void flushCommentKeys() {
        Set<String> keys = REDIS_TEMPLATE.keys(CommentConstants.Cache.PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            REDIS_TEMPLATE.delete(keys);
        }
    }
}

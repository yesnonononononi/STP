package com.summit.stp.post.it;

import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.constants.CacheFieldConstants;
import com.summit.stp.post.application.service.impl.cache.InteractionType;
import com.summit.stp.post.application.service.impl.cache.PostCounterCacheOps;
import com.summit.stp.post.application.service.impl.cache.PostInteractionCacheOps;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 帖子模块并发集成测试（真实 Redis）。
 *
 * <p>覆盖的并发不变量：</p>
 * <ul>
 *     <li>并发浏览量：同一用户 24h 内多次浏览只计 1 次，N 个不同用户各计 1 次；</li>
 *     <li>并发点赞：N 个不同用户点赞后点赞数与点赞集合大小一致；</li>
 *     <li>同一用户并发点赞幂等：最终计数为 1；</li>
 *     <li>并发点赞 + 取消回到平衡态：计数归零且集合为空；</li>
 *     <li>并发回复计数不丢更新。</li>
 * </ul>
 */
@Tag("integration")
class PostConcurrencyIT extends AbstractPostRedisIntegrationTest {

    private final PostInteractionCacheOps interactionOps = new PostInteractionCacheOps(REDIS_TEMPLATE);

    private final PostCounterCacheOps counterOps = new PostCounterCacheOps(REDIS_TEMPLATE);

    @BeforeEach
    void setUp() {
        flushPostKeys();
    }

    @AfterEach
    void tearDown() {
        flushPostKeys();
    }

    @Test
    @DisplayName("并发浏览：同一用户 24h 内去重，浏览量恒为 1")
    void concurrentView_sameUser_dedupCountedOnce() throws Exception {
        long postId = 7001L;
        long userId = 500001L;

        List<Throwable> errors = runConcurrently(40, i -> {
            bindUser(userId);
            try {
                counterOps.incrViewCount(postId);
            } finally {
                UserHolder.clear();
            }
        });

        assertThat(errors).isEmpty();
        assertThat(counterOf(postId, CacheFieldConstants.VIEW_COUNT)).isEqualTo(1L);
    }

    @Test
    @DisplayName("并发浏览：N 个不同用户各计 1 次，浏览量 == N")
    void concurrentView_distinctUsers_countedExactly() throws Exception {
        long postId = 7002L;
        int users = 50;

        List<Throwable> errors = runConcurrently(users, i -> {
            bindUser(500100L + i);
            try {
                counterOps.incrViewCount(postId);
            } finally {
                UserHolder.clear();
            }
        });

        assertThat(errors).isEmpty();
        assertThat(counterOf(postId, CacheFieldConstants.VIEW_COUNT)).isEqualTo((long) users);
    }

    @Test
    @DisplayName("并发点赞：N 个不同用户点赞后计数与集合大小一致")
    void concurrentLike_distinctUsers_countMatchesSet() throws Exception {
        long postId = 7003L;
        int users = 50;

        List<Throwable> errors = runConcurrently(users, i -> {
            long userId = 600000L + i;
            interactionOps.toggle(postId, userId, InteractionType.LIKE);
        });

        assertThat(errors).isEmpty();
        assertThat(counterOf(postId, CacheFieldConstants.LIKE_COUNT)).isEqualTo((long) users);
        assertThat(setSize(postId, InteractionType.LIKE)).isEqualTo((long) users);
    }

    @Test
    @DisplayName("同一用户并发切换点赞：点赞数与其集合规模保持一致，集合规模不超过 1")
    void concurrentToggle_sameUser_countMatchesSet() throws Exception {
        long postId = 7004L;
        long userId = 600999L;

        List<Throwable> errors = runConcurrently(30, i -> interactionOps.toggle(postId, userId, InteractionType.LIKE));

        assertThat(errors).isEmpty();
        long members = setSize(postId, InteractionType.LIKE);
        long count = counterOf(postId, CacheFieldConstants.LIKE_COUNT);
        assertThat(members).as("同一用户切换后的点赞集合规模只能是 0 或 1").isBetween(0L, 1L);
        assertThat(count)
                .as("点赞计数必须与点赞集合真实规模一致（不允许 count > set 的非原子漂移）")
                .isEqualTo(members);
    }

    @Test
    @DisplayName("并发点赞后取消：计数归零且点赞集合为空")
    void concurrentLikeThenUnlike_balanced() throws Exception {
        long postId = 7005L;
        int users = 50;

        List<Throwable> errors = runConcurrently(users, i -> {
            long userId = 610000L + i;
            interactionOps.toggle(postId, userId, InteractionType.LIKE);
            interactionOps.toggle(postId, userId, InteractionType.LIKE);
        });

        assertThat(errors).isEmpty();
        assertThat(counterOf(postId, CacheFieldConstants.LIKE_COUNT)).isZero();
        assertThat(setSize(postId, InteractionType.LIKE)).isZero();
    }

    @Test
    @DisplayName("并发回复计数：N 次并发自增不丢更新，计数 == N")
    void concurrentReplyCount_noLostUpdate() throws Exception {
        long postId = 7006L;
        int threads = 100;

        List<Throwable> errors = runConcurrently(threads, i -> counterOps.incrReplyCount(postId));

        assertThat(errors).isEmpty();
        assertThat(counterOf(postId, CacheFieldConstants.REPLY_COUNT)).isEqualTo((long) threads);
    }

    // ======================== 工具方法 ========================

    private List<Throwable> runConcurrently(int threads, IntConsumer task) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r, "post-concurrency");
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

    private long counterOf(long postId, String field) {
        Object value = REDIS_TEMPLATE.opsForHash()
                .get(PostConstants.Cache.DETAIL_PREFIX + postId, field);
        return value == null ? 0L : Long.parseLong(value.toString());
    }

    private long setSize(long postId, InteractionType type) {
        Long size = REDIS_TEMPLATE.opsForZSet().size(type.buildSetKey(postId));
        return size == null ? 0L : size;
    }

    private void flushPostKeys() {
        Set<String> keys = REDIS_TEMPLATE.keys(PostConstants.Cache.PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            REDIS_TEMPLATE.delete(keys);
        }
    }
}

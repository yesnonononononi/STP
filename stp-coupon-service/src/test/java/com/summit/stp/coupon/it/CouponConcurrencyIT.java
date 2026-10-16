package com.summit.stp.coupon.it;

import com.summit.stp.activity.application.service.CouponActivityAppService;
import com.summit.stp.activity.application.service.CouponActivityCacheProvider;
import com.summit.stp.activity.domain.model.CouponActivity;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.coupon.infrastructure.constants.CouponConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 优惠券模块并发集成测试（真实 Redis + MySQL + RabbitMQ）。
 *
 * <p>覆盖三类并发竞争不变量：</p>
 * <ul>
 *     <li>限量库存并发抢购不得超卖（成功领取数 == 库存）；</li>
 *     <li>同一用户并发重复领取只有 1 次成功（幂等 / 一人一张）；</li>
 *     <li>库存扣减与回滚并发交错后库存守恒、不出现负数。</li>
 * </ul>
 *
 * <p>所有用例均使用固定线程池 + {@link CountDownLatch} 保证多线程“同一瞬间”起跑，
 * 断言并发结束后的最终一致性，而非顺序调用。</p>
 */
@Tag("integration")
class CouponConcurrencyIT extends AbstractCouponIntegrationTest {

    private static final long COUPON_ID_BASE = 9_000_000L;

    @Autowired
    private CouponActivityCacheProvider cacheProvider;

    @Autowired
    private CouponActivityAppService activityAppService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    private JdbcTemplate jdbc() {
        if (jdbcTemplate == null) {
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        return jdbcTemplate;
    }

    /** 幂等建表：coupon 模块自带的 init 脚本未包含 coupon_activity，这里补齐以支撑端到端领券用例。 */
    @BeforeEach
    void ensureActivityTable() {
        jdbc().execute("CREATE TABLE IF NOT EXISTS coupon_activity ("
                + "id bigint NOT NULL AUTO_INCREMENT,"
                + "coupon_id bigint NOT NULL,"
                + "name varchar(128) NOT NULL,"
                + "stock int DEFAULT NULL,"
                + "type int DEFAULT NULL,"
                + "activity_start_time datetime DEFAULT NULL,"
                + "activity_end_time datetime DEFAULT NULL,"
                + "status int DEFAULT NULL,"
                + "create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + "PRIMARY KEY (id),"
                + "KEY idx_activity_coupon_id (coupon_id)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    @AfterEach
    void cleanUp() {
        Set<String> keys = stringRedisTemplate.keys(CouponConstants.Cache.PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
        jdbc().update("DELETE FROM user_coupon WHERE user_id >= 100000");
        jdbc().update("DELETE FROM coupon_activity WHERE id >= ?", COUPON_ID_BASE);
        jdbc().update("DELETE FROM coupon WHERE id >= ?", COUPON_ID_BASE);
    }

    // ======================== 用例 ========================

    @Test
    @DisplayName("并发抢限量库存：成功领取数严格等于库存且不超卖")
    void concurrentSeckill_noOverSell() throws Exception {
        long couponId = COUPON_ID_BASE + 1;
        long activityId = COUPON_ID_BASE + 1;
        int stock = 10;
        int threads = 100;

        cacheProvider.preWarmStock(newActivity(activityId, couponId, stock));
        assertThat(stockOf(activityId)).isEqualTo(stock);

        AtomicInteger success = new AtomicInteger();
        List<Throwable> errors = runConcurrently(threads, i -> {
            long userId = 100000L + i;
            bindUser(userId);
            try {
                if (cacheProvider.deductStock(couponId, activityId)) {
                    success.incrementAndGet();
                }
            } catch (Exception expectedConcurrencyFailure) {
                // 库存不足 / 已达上限 属于并发失败分支，不是错误
            } finally {
                UserHolder.clear();
            }
        });

        assertThat(errors).as("并发线程不应抛出未预期异常").isEmpty();
        assertThat(success.get()).as("成功领取数必须等于库存").isEqualTo(stock);
        assertThat(stockOf(activityId)).as("最终库存应被抢空").isZero();
    }

    @Test
    @DisplayName("同一用户并发重复领取：仅 1 次成功（一人一张幂等）")
    void concurrentSameUser_onlyOneSucceeds() throws Exception {
        long couponId = COUPON_ID_BASE + 2;
        long activityId = COUPON_ID_BASE + 2;
        int stock = 50;
        int threads = 30;
        long userId = 100888L;

        cacheProvider.preWarmStock(newActivity(activityId, couponId, stock));

        AtomicInteger success = new AtomicInteger();
        List<Throwable> errors = runConcurrently(threads, i -> {
            bindUser(userId);
            try {
                if (cacheProvider.deductStock(couponId, activityId)) {
                    success.incrementAndGet();
                }
            } catch (Exception expectedLimitFailure) {
                // 已达个人上限
            } finally {
                UserHolder.clear();
            }
        });

        assertThat(errors).isEmpty();
        assertThat(success.get()).as("同一用户并发领取只能成功一次").isEqualTo(1);
        assertThat(stockOf(activityId)).as("库存只应被扣减 1").isEqualTo(stock - 1);
    }

    @Test
    @DisplayName("扣减与回滚并发交错：库存守恒且不出现负数")
    void concurrentDeductAndRollback_consistent() throws Exception {
        long couponId = COUPON_ID_BASE + 3;
        long activityId = COUPON_ID_BASE + 3;
        int stock = 100;
        int threads = 50;

        cacheProvider.preWarmStock(newActivity(activityId, couponId, stock));

        List<Throwable> errors = runConcurrently(threads, i -> {
            long userId = 200000L + i;
            bindUser(userId);
            try {
                boolean deducted = cacheProvider.deductStock(couponId, activityId);
                if (deducted) {
                    cacheProvider.increaseStock(couponId, userId, activityId);
                }
            } finally {
                UserHolder.clear();
            }
        });

        assertThat(errors).isEmpty();
        assertThat(stockOf(activityId)).as("扣减 + 等量回滚后库存应守恒").isEqualTo(stock);
    }

    @Test
    @DisplayName("端到端并发领券：成功数 == 库存（真实 Redis 原子扣减）")
    void concurrentReceiveActivityCoupon_noOverSell() throws Exception {
        long couponId = COUPON_ID_BASE + 11;
        long activityId = COUPON_ID_BASE + 11;
        int stock = 10;
        int threads = 60;

        insertCoupon(couponId, "IT-并发券");
        insertActivity(activityId, couponId, "IT-并发活动", stock);
        cacheProvider.preWarmStock(newActivity(activityId, couponId, stock));

        AtomicInteger success = new AtomicInteger();
        List<Throwable> errors = runConcurrently(threads, i -> {
            long userId = 300000L + i;
            bindUser(userId);
            try {
                Result<Void> result = activityAppService.receiveActivityCoupon(activityId);
                if (result != null && result.isSuccess()) {
                    success.incrementAndGet();
                }
            } finally {
                UserHolder.clear();
            }
        });

        assertThat(errors).isEmpty();
        assertThat(success.get()).as("端到端并发领券成功数必须等于库存").isEqualTo(stock);
        assertThat(stockOf(activityId)).isZero();
    }

    @Test
    @DisplayName("端到端同一用户并发领券：仅 1 次成功")
    void concurrentReceiveActivityCoupon_sameUserOnce() throws Exception {
        // 本用例会暴露 CouponActivityCacheProviderImpl.cacheUserLimit 的 "hasKey 检查 + putAndExpire(UPSERT)" 竞态：并发下可能把已为 1 的计数重置为 0，导致同一用户成功领取超过 1 张。
        long couponId = COUPON_ID_BASE + 12;
        long activityId = COUPON_ID_BASE + 12;
        int stock = 50;
        int threads = 30;
        long userId = 300999L;

        insertCoupon(couponId, "IT-并发券-单人");
        insertActivity(activityId, couponId, "IT-并发活动-单人", stock);
        cacheProvider.preWarmStock(newActivity(activityId, couponId, stock));

        AtomicInteger success = new AtomicInteger();
        List<Throwable> errors = runConcurrently(threads, i -> {
            bindUser(userId);
            try {
                Result<Void> result = activityAppService.receiveActivityCoupon(activityId);
                if (result != null && result.isSuccess()) {
                    success.incrementAndGet();
                }
            } finally {
                UserHolder.clear();
            }
        });

        assertThat(errors).isEmpty();
        assertThat(success.get()).as("同一用户并发领券只能成功一次").isEqualTo(1);
    }

    // ======================== 工具方法 ========================

    /**
     * 使用固定线程池 + 双闩锁让所有任务同时起跑，等待全部结束后收集异常。
     *
     * @param threads 并发线程数
     * @param task    线程体，入参为线程下标
     * @return 线程执行过程中抛出的所有异常（含断言失败）
     */
    private List<Throwable> runConcurrently(int threads, IntConsumer task) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r, "coupon-concurrency");
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

    private CouponActivity newActivity(long activityId, long couponId, int stock) {
        LocalDateTime now = LocalDateTime.now();
        return CouponActivity.builder()
                .id(activityId)
                .couponId(couponId)
                .name("IT-并发活动")
                .stock(stock)
                .type(CouponActivity.Type.SECKILL)
                .status(1)
                .activityStartTime(now.minusMinutes(1))
                .activityEndTime(now.plusHours(1))
                .createTime(now)
                .build();
    }

    private int stockOf(long activityId) {
        Object value = stringRedisTemplate.opsForHash()
                .get(CouponConstants.Cache.STOCK, String.valueOf(activityId));
        return value == null ? 0 : Integer.parseInt(value.toString());
    }

    private void insertCoupon(long couponId, String name) {
        jdbc().update("INSERT INTO coupon(id,name,discount,amount,type,status,scope_type,time_type,"
                        + "valid_days,valid_hours,description) VALUES(?,?,?,?,?,?,?,?,?,?,?)",
                couponId, name, new BigDecimal("0.90"), new BigDecimal("10.00"),
                1, 1, 1, 2, 7, 168, "concurrency-it");
    }

    private void insertActivity(long activityId, long couponId, String name, int stock) {
        LocalDateTime now = LocalDateTime.now();
        jdbc().update("INSERT INTO coupon_activity(id,coupon_id,name,stock,type,"
                        + "activity_start_time,activity_end_time,status) VALUES(?,?,?,?,?,?,?,?)",
                activityId, couponId, name, stock, 2,
                Timestamp.valueOf(now.minusMinutes(1)),
                Timestamp.valueOf(now.plusHours(1)), 1);
    }
}

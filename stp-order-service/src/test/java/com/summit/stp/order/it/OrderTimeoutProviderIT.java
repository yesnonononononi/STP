package com.summit.stp.order.it;

import com.summit.stp.coupon.api.client.CouponFeignClient;
import com.summit.stp.order.api.client.OrderFeignClient;
import com.summit.stp.order.order.application.service.OrderTimeoutProvider;
import com.summit.stp.order.order.infrastructure.constants.OrderConstants;
import com.summit.stp.payment.api.client.PayFeignClient;
import com.summit.stp.user.api.client.MemberFeignClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 订单超时 Redis ZSet 集成测试（真实 Redis）。
 */
@Tag("integration")
class OrderTimeoutProviderIT extends AbstractOrderIntegrationTest {

    @MockitoBean
    private CouponFeignClient couponFeignClient;

    @MockitoBean
    private MemberFeignClient memberFeignClient;

    @MockitoBean
    private PayFeignClient payFeignClient;

    @MockitoBean
    private OrderFeignClient orderFeignClient;

    @Autowired
    private OrderTimeoutProvider orderTimeoutProvider;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private Double scoreOf(long orderId) {
        return stringRedisTemplate.opsForZSet()
                .score(OrderConstants.Cache.TIMEOUT_ZSET, String.valueOf(orderId));
    }

    @Test
    @DisplayName("registerTimeout：写入真实 zset，score 为超时毫秒时间戳")
    void registerTimeout_writesScore() {
        long orderId = 700_000_001L;
        Timestamp timeoutTime = new Timestamp(System.currentTimeMillis() + 120_000L);

        orderTimeoutProvider.registerTimeout(timeoutTime, orderId);

        assertThat(scoreOf(orderId)).isNotNull();
        assertThat(scoreOf(orderId).longValue()).isEqualTo(timeoutTime.getTime());
    }

    @Test
    @DisplayName("registerTimeout：重复注册以最新时间为准")
    void registerTimeout_overwrite() {
        long orderId = 700_000_002L;
        Timestamp first = new Timestamp(System.currentTimeMillis() + 30_000L);
        Timestamp second = new Timestamp(System.currentTimeMillis() + 90_000L);

        orderTimeoutProvider.registerTimeout(first, orderId);
        orderTimeoutProvider.registerTimeout(second, orderId);

        assertThat(scoreOf(orderId).longValue()).isEqualTo(second.getTime());
    }

    @Test
    @DisplayName("cancelTimeout：从真实 zset 移除")
    void cancelTimeout_removes() {
        long orderId = 700_000_003L;
        orderTimeoutProvider.registerTimeout(new Timestamp(System.currentTimeMillis() + 60_000L), orderId);
        assertThat(scoreOf(orderId)).isNotNull();

        orderTimeoutProvider.cancelTimeout(orderId);

        assertThat(scoreOf(orderId)).isNull();
    }

    @Test
    @DisplayName("cancelTimeouts：批量移除多个成员")
    void cancelTimeouts_batchRemove() {
        long a = 700_000_011L;
        long b = 700_000_012L;
        long c = 700_000_013L;
        Timestamp timeout = new Timestamp(System.currentTimeMillis() + 60_000L);
        orderTimeoutProvider.registerTimeout(timeout, a);
        orderTimeoutProvider.registerTimeout(timeout, b);
        orderTimeoutProvider.registerTimeout(timeout, c);

        orderTimeoutProvider.cancelTimeouts(List.of(a, b));

        assertThat(scoreOf(a)).isNull();
        assertThat(scoreOf(b)).isNull();
        assertThat(scoreOf(c)).isNotNull();
    }
}

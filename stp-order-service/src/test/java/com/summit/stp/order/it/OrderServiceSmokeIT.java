package com.summit.stp.order.it;

import com.summit.stp.common.application.domain.model.PayType;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.coupon.api.client.CouponFeignClient;
import com.summit.stp.order.api.client.OrderFeignClient;
import com.summit.stp.order.order.application.service.OrderTimeoutProvider;
import com.summit.stp.order.order.domain.model.Order;
import com.summit.stp.order.order.domain.model.OrderStatus;
import com.summit.stp.order.order.domain.repository.OrderRepository;
import com.summit.stp.order.order.infrastructure.constants.OrderConstants;
import com.summit.stp.payment.api.client.PayFeignClient;
import com.summit.stp.user.api.client.MemberFeignClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 订单服务冒烟集成测试。
 *
 * <p>验证目标：</p>
 * <ol>
 *   <li>Spring 上下文能在真实 MySQL / RabbitMQ / Redis 容器下完整启动；</li>
 *   <li>{@link OrderRepositoryImpl} 能通过真实 SQL 映射保存并查询 Order；</li>
 *   <li>{@link com.summit.stp.order.order.application.service.impl.RedisOrderTimeoutProvider}
 *       能向真实 Redis 注册/取消订单超时任务。</li>
 * </ol>
 *
 * <p>远程 Feign 客户端使用 {@code @MockitoBean} 替换，避免 IT 依赖其它微服务。</p>
 */
@Tag("integration")
class OrderServiceSmokeIT extends AbstractOrderIntegrationTest {

    private static final long CREATOR_ID = 10001L;

    @MockitoBean
    private CouponFeignClient couponFeignClient;

    @MockitoBean
    private MemberFeignClient memberFeignClient;

    @MockitoBean
    private PayFeignClient payFeignClient;

    @MockitoBean
    private OrderFeignClient orderFeignClient;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderTimeoutProvider orderTimeoutProvider;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @AfterEach
    void tearDown() {
        UserHolder.clear();
    }

    @Test
    @DisplayName("集成冒烟：Spring 上下文启动且核心 Bean 就绪")
    void contextLoadsAndBeansReady() {
        assertThat(orderRepository).isNotNull();
        assertThat(orderTimeoutProvider).isNotNull();
        assertThat(stringRedisTemplate).isNotNull();
        assertThat(MYSQL.isRunning()).isTrue();
        assertThat(RABBITMQ.isRunning()).isTrue();
        assertThat(REDIS.isRunning()).isTrue();
    }

    @Test
    @DisplayName("集成冒烟：OrderRepositoryImpl 在真实 MySQL 中保存并查询 Order")
    void saveAndQueryOrder() {
        UserHolder.setUser(UserSession.builder().id(CREATOR_ID).username("it-user").build());

        long now = System.currentTimeMillis();
        Order order = Order.builder()
                .creatorId(CREATOR_ID)
                .amount(new BigDecimal("99.00"))
                .payType(PayType.WX_PAY)
                .packageId(2001L)
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .discountAmount(new BigDecimal("1.00"))
                .status(OrderStatus.PENDING)
                .createTime(new Timestamp(now))
                .updateTime(new Timestamp(now))
                .timeoutTime(new Timestamp(now + 600_000L))
                .build();

        orderRepository.save(order);

        List<Order> history = orderRepository.queryHistoryOrders(1, 10, null);
        assertThat(history).isNotEmpty();

        Order persisted = history.get(0);
        assertThat(persisted.getId()).isNotNull();
        assertThat(persisted.getCreatorId()).isEqualTo(CREATOR_ID);
        assertThat(persisted.getAmount()).isEqualByComparingTo("99.00");
        assertThat(persisted.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(persisted.getPayType()).isEqualTo(PayType.WX_PAY);

        // 复验按主键查询的真实 SQL 映射
        Order byId = orderRepository.findOrderById(persisted.getId());
        assertThat(byId).isNotNull();
        assertThat(byId.getId()).isEqualTo(persisted.getId());
        assertThat(byId.getUnitPrice()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("集成冒烟：RedisOrderTimeoutProvider 在真实 Redis 中注册并取消超时任务")
    void registerAndCancelOrderTimeout() {
        long orderId = 555_000_111L;
        Timestamp timeoutTime = new Timestamp(System.currentTimeMillis() + 60_000L);

        orderTimeoutProvider.registerTimeout(timeoutTime, orderId);

        Double score = stringRedisTemplate.opsForZSet()
                .score(OrderConstants.Cache.TIMEOUT_ZSET, String.valueOf(orderId));
        assertThat(score).isNotNull();
        assertThat(score.longValue()).isEqualTo(timeoutTime.getTime());

        orderTimeoutProvider.cancelTimeout(orderId);

        Double removed = stringRedisTemplate.opsForZSet()
                .score(OrderConstants.Cache.TIMEOUT_ZSET, String.valueOf(orderId));
        assertThat(removed).isNull();
    }
}

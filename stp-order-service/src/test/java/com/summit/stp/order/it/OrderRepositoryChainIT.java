package com.summit.stp.order.it;

import com.summit.stp.common.application.domain.model.PayType;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.coupon.api.client.CouponFeignClient;
import com.summit.stp.order.api.client.OrderFeignClient;
import com.summit.stp.order.order.domain.model.Order;
import com.summit.stp.order.order.domain.model.OrderStatus;
import com.summit.stp.order.order.domain.repository.OrderRepository;
import com.summit.stp.payment.api.client.PayFeignClient;
import com.summit.stp.user.api.client.MemberFeignClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 订单仓储链路集成测试（真实 MySQL）。
 *
 * <p>覆盖：</p>
 * <ol>
 *   <li>{@code OrderRepositoryImpl.save} -> {@code findOrderById} 字段映射；</li>
 *   <li>{@code ackOrderIfWaitPay} 待支付 -> 已支付（含幂等/非待支付不变）；</li>
 *   <li>{@code batchUpdate} 批量关单（仅作用于待支付订单）。</li>
 * </ol>
 */
@Tag("integration")
class OrderRepositoryChainIT extends AbstractOrderIntegrationTest {

    private static final long CREATOR_A = 90001L;
    private static final long CREATOR_B = 90002L;
    private static final long CREATOR_C = 90003L;

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

    @AfterEach
    void tearDown() {
        UserHolder.clear();
    }

    private Order newOrder(long creatorId, OrderStatus status, BigDecimal amount) {
        long now = System.currentTimeMillis();
        return Order.builder()
                .creatorId(creatorId)
                .amount(amount)
                .payType(PayType.ALI_PAY)
                .packageId(2001L)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .discountAmount(new BigDecimal("10.00"))
                .couponId(5555L)
                .status(status)
                .createTime(new Timestamp(now))
                .updateTime(new Timestamp(now))
                .timeoutTime(new Timestamp(now + 600_000L))
                .build();
    }

    /** 保存订单并回读其自增主键（通过当前用户的订单历史查询）。 */
    private Order saveAndReload(Order order) {
        UserHolder.setUser(UserSession.builder().id(order.getCreatorId()).username("it").build());
        orderRepository.save(order);
        List<Order> history = orderRepository.queryHistoryOrders(1, 10, null);
        assertThat(history).isNotEmpty();
        return history.get(0);
    }

    @Test
    @DisplayName("save -> findOrderById：字段映射正确")
    void saveAndFindById_mapping() {
        Order persisted = saveAndReload(newOrder(CREATOR_A, OrderStatus.PENDING, new BigDecimal("189.00")));

        assertThat(persisted.getId()).isNotNull();

        Order found = orderRepository.findOrderById(persisted.getId());
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(persisted.getId());
        assertThat(found.getCreatorId()).isEqualTo(CREATOR_A);
        assertThat(found.getAmount()).isEqualByComparingTo("189.00");
        assertThat(found.getPayType()).isEqualTo(PayType.ALI_PAY);
        assertThat(found.getPackageId()).isEqualTo(2001L);
        assertThat(found.getQuantity()).isEqualTo(2);
        assertThat(found.getUnitPrice()).isEqualByComparingTo("100.00");
        assertThat(found.getDiscountAmount()).isEqualByComparingTo("10.00");
        assertThat(found.getCouponId()).isEqualTo(5555L);
        assertThat(found.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(found.getCreateTime()).isNotNull();
        assertThat(found.getTimeoutTime()).isNotNull();
        // to / sign 为非持久化字段（@TableField(exist=false)）
        assertThat(found.getTo()).isNull();
        assertThat(found.getSign()).isNull();
    }

    @Test
    @DisplayName("findOrderById：不存在的订单返回 null")
    void findById_notExist() {
        assertThat(orderRepository.findOrderById(888_888_888_888L)).isNull();
    }

    @Test
    @DisplayName("ackOrderIfWaitPay：待支付置为已支付，且重复调用幂等")
    void ackOrderIfWaitPay_pendingToPaid() {
        Order persisted = saveAndReload(newOrder(CREATOR_B, OrderStatus.PENDING, new BigDecimal("50.00")));
        Long id = persisted.getId();

        orderRepository.ackOrderIfWaitPay(id);
        assertThat(orderRepository.findOrderById(id).getStatus()).isEqualTo(OrderStatus.PAID);

        // 再次确认应保持 PAID（expectStatus=PENDING 条件不再命中）
        orderRepository.ackOrderIfWaitPay(id);
        assertThat(orderRepository.findOrderById(id).getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("ackOrderIfWaitPay：非待支付订单不受影响")
    void ackOrderIfWaitPay_nonPendingUnchanged() {
        Order persisted = saveAndReload(newOrder(CREATOR_C, OrderStatus.CANCELLED, new BigDecimal("30.00")));
        Long id = persisted.getId();

        orderRepository.ackOrderIfWaitPay(id);

        assertThat(orderRepository.findOrderById(id).getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("batchUpdate：批量将待支付订单置为已取消")
    void batchUpdate_cancelsPending() {
        Order o1 = saveAndReload(newOrder(90011L, OrderStatus.PENDING, new BigDecimal("11.00")));
        Order o2 = saveAndReload(newOrder(90012L, OrderStatus.PENDING, new BigDecimal("22.00")));

        orderRepository.batchUpdate(List.of(o1, o2));

        assertThat(orderRepository.findOrderById(o1.getId()).getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(orderRepository.findOrderById(o2.getId()).getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("batchUpdate：已支付订单不被批量关单覆盖")
    void batchUpdate_skipsNonPending() {
        Order paid = saveAndReload(newOrder(90013L, OrderStatus.PAID, new BigDecimal("33.00")));

        orderRepository.batchUpdate(List.of(paid));

        assertThat(orderRepository.findOrderById(paid.getId()).getStatus()).isEqualTo(OrderStatus.PAID);
    }
}

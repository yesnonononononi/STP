package com.summit.stp.order.it;

import com.summit.stp.common.application.domain.event.OrderPaidEvent;
import com.summit.stp.common.application.domain.event.PaySuccessEvent;
import com.summit.stp.common.application.domain.model.PayType;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.coupon.api.client.CouponFeignClient;
import com.summit.stp.order.api.client.OrderFeignClient;
import com.summit.stp.order.order.application.service.EventPublishProvider;
import com.summit.stp.order.order.domain.model.Order;
import com.summit.stp.order.order.domain.model.OrderStatus;
import com.summit.stp.order.order.domain.repository.OrderRepository;
import com.summit.stp.payment.api.client.PayFeignClient;
import com.summit.stp.user.api.client.MemberFeignClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * 订单支付成功链路集成测试（真实 RabbitMQ）。
 *
 * <p>覆盖：</p>
 * <ol>
 *   <li>{@code EventPublishProviderImpl.publish(OrderPaidEvent)} 经真实 RabbitMQ
 *       投递到会员权益交换机/路由键，消息体字段完整；</li>
 *   <li>{@code OrderPaySuccessQueueListener} 消费 {@code PaySuccessEvent}，
 *       在真实 MySQL 中把订单置为已支付（异步等待，使用 Awaitility 轮询）。</li>
 * </ol>
 */
@Tag("integration")
class OrderPaySuccessEventIT extends AbstractOrderIntegrationTest {

    private static final long CREATOR_ID = 90101L;

    @MockitoBean
    private CouponFeignClient couponFeignClient;

    @MockitoBean
    private MemberFeignClient memberFeignClient;

    @MockitoBean
    private PayFeignClient payFeignClient;

    @MockitoBean
    private OrderFeignClient orderFeignClient;

    @Autowired
    private EventPublishProvider eventPublishProvider;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @BeforeEach
    void purgeQueues() {
        amqpAdmin.purgeQueue(MqConstants.Member.QUEUE);
        amqpAdmin.purgeQueue(MqConstants.Pay.QUEUE_SUCCESS);
    }

    @AfterEach
    void tearDown() {
        UserHolder.clear();
    }

    @Test
    @DisplayName("EventPublishProvider.publish：OrderPaidEvent 投递到会员交换机，消息体字段完整")
    void publishOrderPaidEvent_routedToMemberQueue() {
        long now = System.currentTimeMillis();
        OrderPaidEvent event = OrderPaidEvent.builder()
                .orderId(660_000_123L)
                .creatorId(CREATOR_ID)
                .packageId(2001L)
                .quantity(1)
                .payTime(new Timestamp(now))
                .build();

        eventPublishProvider.publish(event);

        Object received = rabbitTemplate.receiveAndConvert(MqConstants.Member.QUEUE, 15_000L);
        assertThat(received).isInstanceOf(OrderPaidEvent.class);
        OrderPaidEvent body = (OrderPaidEvent) received;
        assertThat(body.getOrderId()).isEqualTo(660_000_123L);
        assertThat(body.getCreatorId()).isEqualTo(CREATOR_ID);
        assertThat(body.getPackageId()).isEqualTo(2001L);
        assertThat(body.getQuantity()).isEqualTo(1);
        assertThat(body.getPayTime()).isNotNull();
    }

    @Test
    @DisplayName("OrderPaySuccessQueueListener：消费 PaySuccessEvent 后将订单置为已支付")
    void paySuccessListener_marksOrderPaid() {
        Order order = savePendingOrder();
        Long orderId = order.getId();
        assertThat(orderRepository.findOrderById(orderId).getStatus()).isEqualTo(OrderStatus.PENDING);

        rabbitTemplate.convertAndSend(
                MqConstants.Pay.EXCHANGE, MqConstants.Pay.ROUTING_KEY_SUCCESS, new PaySuccessEvent(orderId));

        await().atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(200))
                .untilAsserted(() -> assertThat(orderRepository.findOrderById(orderId).getStatus())
                        .isEqualTo(OrderStatus.PAID));
    }

    private Order savePendingOrder() {
        UserHolder.setUser(UserSession.builder().id(CREATOR_ID).username("it").build());
        long now = System.currentTimeMillis();
        Order order = Order.builder()
                .creatorId(CREATOR_ID)
                .amount(new BigDecimal("88.00"))
                .payType(PayType.WX_PAY)
                .packageId(2001L)
                .quantity(1)
                .unitPrice(new BigDecimal("88.00"))
                .discountAmount(new BigDecimal("0.00"))
                .status(OrderStatus.PENDING)
                .createTime(new Timestamp(now))
                .updateTime(new Timestamp(now))
                .timeoutTime(new Timestamp(now + 600_000L))
                .build();
        orderRepository.save(order);
        List<Order> history = orderRepository.queryHistoryOrders(1, 10, null);
        assertThat(history).isNotEmpty();
        return history.get(0);
    }
}

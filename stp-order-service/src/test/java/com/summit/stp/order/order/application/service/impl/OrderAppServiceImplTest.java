package com.summit.stp.order.order.application.service.impl;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.application.domain.event.OrderPaidEvent;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.application.domain.model.PayType;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.coupon.api.client.CouponFeignClient;
import com.summit.stp.coupon.api.vo.CouponQueryVO;
import com.summit.stp.order.order.api.dto.OrderCreateRequest;
import com.summit.stp.order.order.application.service.OrderReconciliationAppService;
import com.summit.stp.order.order.application.service.OrderTimeoutProvider;
import com.summit.stp.order.order.application.service.EventPublishProvider;
import com.summit.stp.order.order.application.vo.OrderCreateVO;
import com.summit.stp.order.order.domain.exception.OrderNotFoundException;
import com.summit.stp.order.order.domain.model.Order;
import com.summit.stp.order.order.domain.model.OrderStatus;
import com.summit.stp.order.order.domain.repository.OrderRepository;
import com.summit.stp.payment.api.client.PayFeignClient;import com.summit.stp.user.api.client.MemberFeignClient;
import com.summit.stp.user.api.vo.MemberVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderAppServiceImpl 纯单元测试（Mockito）。
 */
@ExtendWith(MockitoExtension.class)
class OrderAppServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CouponFeignClient couponFeignClient;
    @Mock
    private PayFeignClient payFeignClient;
    @Mock
    private MemberFeignClient memberFeignClient;
    @Mock
    private OrderTimeoutProvider orderTimeoutProvider;
    @Mock
    private EventPublishProvider eventPublishProvider;
    @Mock
    private OrderReconciliationAppService orderReconciliationAppService;

    private OrderAppServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderAppServiceImpl(orderRepository, couponFeignClient, payFeignClient,
                memberFeignClient, orderTimeoutProvider, eventPublishProvider, orderReconciliationAppService);
        UserHolder.setUser(UserSession.builder().id(7L).username("tester").build());
    }

    @AfterEach
    void tearDown() {
        UserHolder.clear();
    }

    private MemberVO member() {
        return MemberVO.builder()
                .id(2001L)
                .typeId(11L)
                .name("VIP")
                .price(new BigDecimal("100.00"))
                .discount(0.9)
                .build();
    }

    private OrderCreateRequest createRequest(Long couponId, Integer payType) {
        OrderCreateRequest req = new OrderCreateRequest();
        req.setPackageId(2001L);
        req.setQuantity(5); // 会被强制置 1
        req.setCouponId(couponId);
        req.setPayType(payType);
        return req;
    }

    // ---------------- createOrder ----------------

    @Test
    @DisplayName("createOrder: 无优惠券 -> 计算折后金额并落库，返回 orderId")
    void createOrder_withoutCoupon() {
        when(memberFeignClient.queryMemberById(2001L)).thenReturn(Result.success(member()));
        when(orderRepository.generateOrderId()).thenReturn(1000L);

        Result<OrderCreateVO> result = service.createOrder(createRequest(null, 1));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData().getOrderId()).isEqualTo(1000L);
        assertThat(result.getData().getEndTime()).isNotNull();

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(saved.getCreatorId()).isEqualTo(7L);
        assertThat(saved.getQuantity()).isEqualTo(1);
        assertThat(saved.getAmount()).isEqualByComparingTo("90.00");
        assertThat(saved.getPayType()).isEqualTo(PayType.WX_PAY);
        verify(couponFeignClient, never()).use(anyLong(), anyLong(), anyLong(), anyLong());
        verify(orderTimeoutProvider).registerTimeout(any(Timestamp.class), eq(1000L));
    }

    @Test
    @DisplayName("createOrder: 有优惠券 -> 调用 use + calculateAmount")
    void createOrder_withCoupon() {
        when(memberFeignClient.queryMemberById(2001L)).thenReturn(Result.success(member()));
        when(orderRepository.generateOrderId()).thenReturn(1000L);
        when(couponFeignClient.use(555L, 1000L, 11L, 2001L)).thenReturn(Result.success());
        when(couponFeignClient.calculateAmount(any(BigDecimal.class), eq(1), eq(555L)))
                .thenReturn(Result.success(new BigDecimal("80.00")));

        Result<OrderCreateVO> result = service.createOrder(createRequest(555L, 1));

        assertThat(result.isSuccess()).isTrue();
        verify(couponFeignClient).use(555L, 1000L, 11L, 2001L);
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("80.00");
        assertThat(captor.getValue().getCouponId()).isEqualTo(555L);
    }

    @Test
    @DisplayName("createOrder: 套餐折扣为 null -> 使用原价")
    void createOrder_withoutMemberDiscount() {
        MemberVO m = member();
        m.setDiscount(null);
        when(memberFeignClient.queryMemberById(2001L)).thenReturn(Result.success(m));
        when(orderRepository.generateOrderId()).thenReturn(1000L);

        service.createOrder(createRequest(null, 1));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("createOrder: 非法 payType -> ParameterException")
    void createOrder_invalidPayType() {
        when(memberFeignClient.queryMemberById(2001L)).thenReturn(Result.success(member()));
        when(orderRepository.generateOrderId()).thenReturn(1000L);

        assertThatThrownBy(() -> service.createOrder(createRequest(null, 99)))
                .isInstanceOf(ParameterException.class);
        verify(orderRepository, never()).save(any());
    }

    // ---------------- cancelOrder ----------------

    @Test
    @DisplayName("cancelOrder: 订单不存在 -> 直接返回")
    void cancelOrder_notFound() {
        when(orderRepository.findOrderById(1L)).thenReturn(null);

        service.cancelOrder(1L);

        verify(orderRepository, never()).updateById(any());
    }

    @Test
    @DisplayName("cancelOrder: 存在且无优惠券 -> 取消并更新")
    void cancelOrder_noCoupon() {
        Order order = Order.builder().id(1L).creatorId(7L).status(OrderStatus.PENDING).build();
        when(orderRepository.findOrderById(1L)).thenReturn(order);

        service.cancelOrder(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).updateById(order);
        verify(orderTimeoutProvider).cancelTimeout(1L);
        verify(couponFeignClient, never()).refund(anyLong());
    }

    @Test
    @DisplayName("cancelOrder: 有优惠券 -> 退回优惠券")
    void cancelOrder_withCoupon() {
        Order order = Order.builder().id(1L).creatorId(7L).status(OrderStatus.PENDING).couponId(88L).build();
        when(orderRepository.findOrderById(1L)).thenReturn(order);

        service.cancelOrder(1L);

        verify(couponFeignClient).refund(88L);
    }

    @Test
    @DisplayName("cancelOrder: 优惠券退回异常 -> 向上抛出")
    void cancelOrder_couponRefundThrows() {
        Order order = Order.builder().id(1L).creatorId(7L).status(OrderStatus.PENDING).couponId(88L).build();
        when(orderRepository.findOrderById(1L)).thenReturn(order);
        when(couponFeignClient.refund(88L)).thenThrow(new RuntimeException("refund failed"));

        assertThatThrownBy(() -> service.cancelOrder(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("refund failed");
    }

    // ---------------- cancelOrderTimeout ----------------

    @Test
    @DisplayName("cancelOrderTimeout: 空集合直接返回")
    void cancelOrderTimeout_empty() {
        service.cancelOrderTimeout(List.of());
        service.cancelOrderTimeout(null);

        verify(orderRepository, never()).batchUpdate(any());
    }

    @Test
    @DisplayName("cancelOrderTimeout: 非 PENDING 跳过")
    void cancelOrderTimeout_nonPendingSkipped() {
        Order order = Order.builder().id(1L).creatorId(7L).status(OrderStatus.PAID).build();

        service.cancelOrderTimeout(List.of(order));

        verify(orderRepository, never()).batchUpdate(any());
    }

    @Test
    @DisplayName("cancelOrderTimeout: 对账成功 -> 跳过关单")
    void cancelOrderTimeout_reconcileSuccess() {
        Order order = Order.builder().id(1L).creatorId(7L).status(OrderStatus.PENDING).build();
        when(orderReconciliationAppService.reconcileOrder(1L, true))
                .thenReturn(Result.success("已补单"));

        service.cancelOrderTimeout(List.of(order));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository, never()).batchUpdate(any());
    }

    @Test
    @DisplayName("cancelOrderTimeout: 对账未成功 -> 关单 + 批量更新 + 退券")
    void cancelOrderTimeout_cancelAndRefund() {
        Order order = Order.builder().id(1L).creatorId(7L).status(OrderStatus.PENDING).couponId(88L).build();
        when(orderReconciliationAppService.reconcileOrder(1L, true))
                .thenReturn(Result.error("未支付"));

        service.cancelOrderTimeout(List.of(order));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).batchUpdate(List.of(order));
        verify(couponFeignClient).refund(88L);
    }

    @Test
    @DisplayName("cancelOrderTimeout: 对账抛异常 -> 仍关单")
    void cancelOrderTimeout_reconcileThrows() {
        Order order = Order.builder().id(1L).creatorId(7L).status(OrderStatus.PENDING).build();
        when(orderReconciliationAppService.reconcileOrder(1L, true))
                .thenThrow(new RuntimeException("rpc error"));

        service.cancelOrderTimeout(List.of(order));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).batchUpdate(List.of(order));
    }

    // ---------------- ackOrder ----------------

    @Test
    @DisplayName("ackOrder: 订单不存在 -> ParameterException")
    void ackOrder_notFound() {
        when(orderRepository.findOrderById(1L)).thenReturn(null);

        assertThatThrownBy(() -> service.ackOrder(1L))
                .isInstanceOf(ParameterException.class);
    }

    @Test
    @DisplayName("ackOrder: 正常 -> 标记支付 + 取消超时 + 发布事件")
    void ackOrder_success() {
        Order order = Order.builder().id(1000L).creatorId(7L).quantity(1).packageId(2001L)
                .status(OrderStatus.PENDING).build();
        when(orderRepository.findOrderById(1000L)).thenReturn(order);

        service.ackOrder(1000L);

        verify(orderRepository).ackOrderIfWaitPay(1000L);
        verify(orderTimeoutProvider).cancelTimeout(1000L);
        ArgumentCaptor<OrderPaidEvent> captor = ArgumentCaptor.forClass(OrderPaidEvent.class);
        verify(eventPublishProvider).publish(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(1000L);
    }

    // ---------------- ack ----------------

    @Test
    @DisplayName("ack: 未找到 -> OrderNotFoundException")
    void ack_notFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.ack(1L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("ack: 正常 -> complete + updateById")
    void ack_success() {
        Order order = Order.builder().id(1L).creatorId(7L).status(OrderStatus.PAID).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Result<Void> result = service.ack(1L);

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        verify(orderRepository).updateById(order);
    }

    // ---------------- deleteById ----------------

    @Test
    @DisplayName("deleteById: 非本人 -> ParameterException")
    void deleteById_notOwner() {
        Order order = Order.builder().id(1L).creatorId(9L).status(OrderStatus.PENDING).build();
        when(orderRepository.findOrderById(1L)).thenReturn(order);

        assertThatThrownBy(() -> service.deleteById(1L))
                .isInstanceOf(ParameterException.class);
        verify(orderRepository, never()).deleteById(1L);
    }

    @Test
    @DisplayName("deleteById: 本人 -> 删除")
    void deleteById_owner() {
        Order order = Order.builder().id(1L).creatorId(7L).status(OrderStatus.PENDING).build();
        when(orderRepository.findOrderById(1L)).thenReturn(order);

        service.deleteById(1L);

        verify(orderRepository).deleteById(1L);
    }

    // ---------------- queryById ----------------

    @Test
    @DisplayName("queryById: 订单不存在 -> null")
    void queryById_notFound() {
        when(orderRepository.findOrderById(1L)).thenReturn(null);

        assertThat(service.queryById(1L)).isNull();
    }

    @Test
    @DisplayName("queryById: 正常映射会员与优惠券信息")
    void queryById_mapping() {
        Order order = Order.builder()
                .id(1L).creatorId(7L).status(OrderStatus.PENDING)
                .payType(PayType.ALI_PAY).packageId(2001L).couponId(555L)
                .amount(new BigDecimal("80.00")).unitPrice(new BigDecimal("100.00")).quantity(1)
                .to("acc").createTime(new Timestamp(1_700_000_000_000L))
                .build();
        when(orderRepository.findOrderById(1L)).thenReturn(order);
        when(memberFeignClient.queryMemberById(2001L)).thenReturn(Result.success(member()));
        CouponQueryVO coupon = CouponQueryVO.builder().id(555L).name("满减券").build();
        when(couponFeignClient.queryById(555L)).thenReturn(Result.success(coupon));

        var vo = service.queryById(1L);

        assertThat(vo.getOrderId()).isEqualTo(1L);
        assertThat(vo.getStatus()).isEqualTo("0");
        assertThat(vo.getPayTypeName()).isEqualTo("2");
        assertThat(vo.getMemberName()).isEqualTo("VIP");
        assertThat(vo.getCouponName()).isEqualTo("满减券");
        assertThat(vo.getPayableAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("queryById: 无优惠券 -> couponName 为未使用")
    void queryById_noCoupon() {
        Order order = Order.builder()
                .id(1L).creatorId(7L).status(OrderStatus.PENDING)
                .payType(PayType.WX_PAY).packageId(2001L)
                .amount(new BigDecimal("80.00"))
                .build();
        when(orderRepository.findOrderById(1L)).thenReturn(order);
        when(memberFeignClient.queryMemberById(2001L)).thenReturn(Result.success(member()));

        var vo = service.queryById(1L);

        assertThat(vo.getCouponName()).isEqualTo("未使用优惠券");
        assertThat(vo.getMemberId()).isEqualTo(2001L);
    }
}

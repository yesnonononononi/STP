package com.summit.stp.order.order.application.service.impl;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.service.queue.QueueSender;
import com.summit.stp.common.util.PaymentSignHelper;
import com.summit.stp.order.order.application.vo.PaymentQueryResultVO;
import com.summit.stp.order.order.domain.exception.OrderNotFoundException;
import com.summit.stp.order.order.domain.model.Order;
import com.summit.stp.order.order.domain.model.OrderStatus;
import com.summit.stp.order.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderReconciliationAppServiceImpl 纯单元测试。
 */
@ExtendWith(MockitoExtension.class)
class OrderReconciliationAppServiceImplTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private PaymentSignHelper paymentSignHelper;
    @Mock
    private QueueSender queueSender;
    @Mock
    private OrderRepository orderRepository;

    private OrderReconciliationAppServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderReconciliationAppServiceImpl(restTemplate, paymentSignHelper, queueSender, orderRepository);
        ReflectionTestUtils.setField(service, "pid", 10001);
        ReflectionTestUtils.setField(service, "queryUrl", "http://query");
    }

    private void stubEntity() {
        when(paymentSignHelper.buildFormUrlEncodedEntity(any()))
                .thenReturn(new HttpEntity<>(new LinkedMultiValueMap<String, String>(), new HttpHeaders()));
    }

    @Test
    @DisplayName("本地已 PAID -> 短路返回成功，不查三方")
    void reconcile_localPaid_shortCircuit() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(Order.builder().id(1L).status(OrderStatus.PAID).build()));

        Result<String> result = service.reconcileOrder(1L, true);

        assertThat(result.isSuccess()).isTrue();
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(PaymentQueryResultVO.class));
    }

    @Test
    @DisplayName("本地已完成 -> 短路返回成功")
    void reconcile_localCompleted_shortCircuit() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(Order.builder().id(1L).status(OrderStatus.COMPLETED).build()));

        Result<String> result = service.reconcileOrder(1L, false);

        assertThat(result.isSuccess()).isTrue();
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(PaymentQueryResultVO.class));
    }

    @Test
    @DisplayName("订单不存在 -> OrderNotFoundException")
    void reconcile_notFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reconcileOrder(1L, true))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("三方已支付 + 需要补偿 -> 触发补单并发送 MQ")
    void reconcile_thirdPartyPaid_compensate() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(Order.builder().id(1L).status(OrderStatus.PENDING).build()));
        stubEntity();
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentQueryResultVO.class)))
                .thenReturn(PaymentQueryResultVO.builder().code(0).status(1).build());

        Result<String> result = service.reconcileOrder(1L, true);

        assertThat(result.isSuccess()).isTrue();
        verify(queueSender).send(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("三方已支付但不补偿 -> 不发送 MQ")
    void reconcile_thirdPartyPaid_noCompensate() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(Order.builder().id(1L).status(OrderStatus.PENDING).build()));
        stubEntity();
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentQueryResultVO.class)))
                .thenReturn(PaymentQueryResultVO.builder().code(0).status(1).build());

        Result<String> result = service.reconcileOrder(1L, false);

        assertThat(result.isSuccess()).isTrue();
        verify(queueSender, never()).send(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("三方未支付 -> Result.error")
    void reconcile_thirdPartyUnpaid() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(Order.builder().id(1L).status(OrderStatus.PENDING).build()));
        stubEntity();
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentQueryResultVO.class)))
                .thenReturn(PaymentQueryResultVO.builder().code(0).status(0).build());

        Result<String> result = service.reconcileOrder(1L, true);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrMsg()).contains("订单未支付完成");
    }

    @Test
    @DisplayName("三方查询返回 code!=0 -> BusinessException")
    void reconcile_queryFailed() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(Order.builder().id(1L).status(OrderStatus.PENDING).build()));
        stubEntity();
        when(restTemplate.postForObject(anyString(), any(), eq(PaymentQueryResultVO.class)))
                .thenReturn(PaymentQueryResultVO.builder().code(1).msg("err").build());

        assertThatThrownBy(() -> service.reconcileOrder(1L, true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("查询三方订单状态失败");
    }

    @Test
    @DisplayName("compensateOrder -> 发送 PaySuccessEvent")
    void compensateOrder() {
        Result<Void> result = service.compensateOrder(1L);

        assertThat(result.isSuccess()).isTrue();
        verify(queueSender).send(anyString(), anyString(), any());
    }
}

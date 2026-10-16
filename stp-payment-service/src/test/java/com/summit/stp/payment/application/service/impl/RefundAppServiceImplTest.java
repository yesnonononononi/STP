package com.summit.stp.payment.application.service.impl;

import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.util.PaymentSignHelper;
import com.summit.stp.payment.application.command.RefundCommand;
import com.summit.stp.payment.application.vo.RefundResultVO;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * RefundAppServiceImpl 纯单元测试。
 */
@ExtendWith(MockitoExtension.class)
class RefundAppServiceImplTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private PaymentSignHelper paymentSignHelper;

    private RefundAppServiceImpl refundAppService;

    @BeforeEach
    void setUp() {
        refundAppService = new RefundAppServiceImpl(restTemplate, paymentSignHelper);
        ReflectionTestUtils.setField(refundAppService, "pid", 10001);
        ReflectionTestUtils.setField(refundAppService, "refundUrl", "http://refund");
    }

    private void stubEntity() {
        when(paymentSignHelper.buildFormUrlEncodedEntity(any()))
                .thenReturn(new HttpEntity<>(new LinkedMultiValueMap<String, String>(), new HttpHeaders()));
    }

    @Test
    @DisplayName("refund: tradeNo 与 outTradeNo 同时为空 -> BusinessException")
    void refund_bothOrderNoNull() {
        RefundCommand command = RefundCommand.builder().money(new BigDecimal("1.00")).build();

        assertThatThrownBy(() -> refundAppService.refund(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能同时为空");
    }

    @Test
    @DisplayName("refund: money 为 null -> BusinessException")
    void refund_moneyNull() {
        RefundCommand command = RefundCommand.builder().tradeNo("T1").build();

        assertThatThrownBy(() -> refundAppService.refund(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("退款金额不能为空");
    }

    @Test
    @DisplayName("refund: 正常调用 -> 返回三方响应")
    void refund_success() {
        stubEntity();
        RefundResultVO expected = RefundResultVO.builder().code(0).refundNo("R1").build();
        when(restTemplate.postForObject(eq("http://refund"), any(), eq(RefundResultVO.class)))
                .thenReturn(expected);

        RefundResultVO result = refundAppService.refund(
                RefundCommand.builder().tradeNo("T1").money(new BigDecimal("1.00")).build());

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("refund: 三方返回 null -> BusinessException")
    void refund_nullResponse() {
        stubEntity();
        when(restTemplate.postForObject(eq("http://refund"), any(), eq(RefundResultVO.class))).thenReturn(null);

        assertThatThrownBy(() -> refundAppService.refund(
                RefundCommand.builder().outTradeNo("O1").money(new BigDecimal("1.00")).build()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("三方退款响应为空");
    }

    @Test
    @DisplayName("refund: 底层异常 -> 包装为 BusinessException")
    void refund_wrapsException() {
        stubEntity();
        when(restTemplate.postForObject(eq("http://refund"), any(), eq(RefundResultVO.class)))
                .thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> refundAppService.refund(
                RefundCommand.builder().tradeNo("T1").money(new BigDecimal("1.00")).build()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("三方退款接口请求异常");
    }
}

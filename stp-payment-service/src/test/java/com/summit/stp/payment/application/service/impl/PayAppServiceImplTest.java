package com.summit.stp.payment.application.service.impl;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.application.domain.event.PayFailEvent;
import com.summit.stp.common.application.domain.event.PaySuccessEvent;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.model.PayType;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.util.PaymentSignHelper;
import com.summit.stp.order.api.client.OrderFeignClient;
import com.summit.stp.order.api.vo.OrderQueryVO;
import com.summit.stp.payment.api.dto.PayCommand;
import com.summit.stp.payment.api.vo.PayVO;
import com.summit.stp.payment.application.command.PayCallbackCheckCommand;
import com.summit.stp.payment.application.service.PayEventPublishProvider;
import com.summit.stp.payment.domain.PayDomainService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PayAppServiceImpl 纯单元测试（Mockito）。
 */
@ExtendWith(MockitoExtension.class)
class PayAppServiceImplTest {

    @Mock
    private PayDomainService payService;
    @Mock
    private PayEventPublishProvider payEventPublishProvider;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private PaymentSignHelper paymentSignHelper;
    @Mock
    private OrderFeignClient orderFeignClient;

    private PayAppServiceImpl payAppService;

    private static final String SUCCESS_SYMBOL = "TRADE_SUCCESS";

    @BeforeEach
    void setUp() {
        payAppService = new PayAppServiceImpl(
                payService, payEventPublishProvider, restTemplate, paymentSignHelper, orderFeignClient);
        ReflectionTestUtils.setField(payAppService, "pid", 10001);
        ReflectionTestUtils.setField(payAppService, "notifyUrl", "http://notify");
        ReflectionTestUtils.setField(payAppService, "returnUrl", "http://return");
        ReflectionTestUtils.setField(payAppService, "createUrl", "http://create");
        ReflectionTestUtils.setField(payAppService, "successSymbol", SUCCESS_SYMBOL);
    }

    @AfterEach
    void tearDown() {
        UserHolder.clear();
    }

    private PayCommand command() {
        return PayCommand.builder()
                .username("u")
                .memberName("VIP")
                .payType(PayType.ALI_PAY)
                .amount(new BigDecimal("19.9"))
                .orderId(123L)
                .timestamp(new Timestamp(1_700_000_000_000L))
                .build();
    }

    private void stubGatewayEntity() {
        when(paymentSignHelper.buildFormUrlEncodedEntity(any()))
                .thenReturn(new HttpEntity<>(new LinkedMultiValueMap<String, String>(), new HttpHeaders()));
    }

    @Test
    @DisplayName("pay: 合法响应 + 验签通过 -> 返回 PayVO")
    void pay_success() {
        stubGatewayEntity();
        String json = "{\"code\":0,\"msg\":\"ok\",\"trade_no\":\"t1\",\"pay_type\":\"alipay\","
                + "\"pay_info\":\"http://gateway/pay\",\"timestamp\":\"1700000000\","
                + "\"sign\":\"SIG\",\"sign_type\":\"RSA\"}";
        when(restTemplate.postForObject(eq("http://create"), any(), eq(String.class))).thenReturn(json);
        when(payService.checkSign(anyMap(), anyString())).thenReturn(true);

        PayVO vo = payAppService.pay(command());

        assertThat(vo.getPid()).isEqualTo(10001);
        assertThat(vo.getType()).isEqualTo("alipay");
        assertThat(vo.getOrderId()).isEqualTo(123L);
        assertThat(vo.getMoney()).isEqualTo("19.90");
        assertThat(vo.getMemberName()).isEqualTo("VIP");
        assertThat(vo.getTo()).isEqualTo("http://gateway/pay");
        assertThat(vo.getSign()).isEqualTo("SIG");
        assertThat(vo.getSignType()).isEqualTo("RSA");
        assertThat(vo.getTimestamp()).isEqualTo("1700000000");
    }

    @Test
    @DisplayName("pay: 空响应 -> BusinessException")
    void pay_emptyResponse() {
        stubGatewayEntity();
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn("  ");

        assertThatThrownBy(() -> payAppService.pay(command()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("响应为空");
    }

    @Test
    @DisplayName("pay: null 响应 -> BusinessException")
    void pay_nullResponse() {
        stubGatewayEntity();
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn(null);

        assertThatThrownBy(() -> payAppService.pay(command()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("响应为空");
    }

    @Test
    @DisplayName("pay: 非法 JSON -> BusinessException")
    void pay_invalidJson() {
        stubGatewayEntity();
        when(restTemplate.postForObject(anyString(), any(), eq(String.class))).thenReturn("{not-json");

        assertThatThrownBy(() -> payAppService.pay(command()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("解析三方响应");
    }

    @Test
    @DisplayName("pay: code!=0 -> BusinessException")
    void pay_badCode() {
        stubGatewayEntity();
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("{\"code\":1,\"msg\":\"fail\"}");

        assertThatThrownBy(() -> payAppService.pay(command()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("三方下单失败");
    }

    @Test
    @DisplayName("pay: 验签失败 -> BusinessException")
    void pay_badSign() {
        stubGatewayEntity();
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("{\"code\":0,\"pay_info\":\"x\",\"sign\":\"S\",\"sign_type\":\"RSA\"}");
        when(payService.checkSign(anyMap(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> payAppService.pay(command()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("验签失败");
    }

    @Test
    @DisplayName("pay: 网关调用异常 -> 包装为 BusinessException")
    void pay_gatewayThrows() {
        stubGatewayEntity();
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("connection refused"));

        assertThatThrownBy(() -> payAppService.pay(command()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("请求三方支付网关异常");
    }

    @Test
    @DisplayName("checkResult: 验签失败 -> BusinessException")
    void checkResult_badSign() {
        PayCallbackCheckCommand cmd = new PayCallbackCheckCommand();
        cmd.setTrade_status(SUCCESS_SYMBOL);
        cmd.setOut_trade_no(123L);
        when(payService.checkSign(anyMap(), any())).thenReturn(false);

        assertThatThrownBy(() -> payAppService.checkResult(cmd))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("签名验证失败");
        verify(payEventPublishProvider, never()).publish(any(PaySuccessEvent.class));
    }

    @Test
    @DisplayName("checkResult: trade_status==successSymbol -> 发布 PaySuccessEvent")
    void checkResult_successPublishesSuccessEvent() {
        PayCallbackCheckCommand cmd = new PayCallbackCheckCommand();
        cmd.setTrade_status(SUCCESS_SYMBOL);
        cmd.setOut_trade_no(123L);
        cmd.setSign("S");
        when(payService.checkSign(anyMap(), any())).thenReturn(true);

        payAppService.checkResult(cmd);

        ArgumentCaptor<PaySuccessEvent> captor = ArgumentCaptor.forClass(PaySuccessEvent.class);
        verify(payEventPublishProvider).publish(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(123L);
    }

    @Test
    @DisplayName("checkResult: trade_status!=successSymbol -> 发布 PayFailEvent")
    void checkResult_publishesFailEvent() {
        PayCallbackCheckCommand cmd = new PayCallbackCheckCommand();
        cmd.setTrade_status("TRADE_FAIL");
        cmd.setOut_trade_no(123L);
        cmd.setSign("S");
        when(payService.checkSign(anyMap(), any())).thenReturn(true);

        payAppService.checkResult(cmd);

        ArgumentCaptor<PayFailEvent> captor = ArgumentCaptor.forClass(PayFailEvent.class);
        verify(payEventPublishProvider).publish(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(123L);
        assertThat(captor.getValue().getReason()).isEqualTo("TRADE_FAIL");
    }

    @Test
    @DisplayName("getType: 返回包含 wxpay/alipay 的列表")
    void getType() {
        Result<List<String>> result = payAppService.getType();

        assertThat(result.getData()).containsExactlyInAnyOrder("wxpay", "alipay");
    }

    @Test
    @DisplayName("toPay: 订单不存在 -> Result.error")
    void toPay_orderNotFound() {
        when(orderFeignClient.findById(1L)).thenReturn(Result.success(null));

        Result<String> result = payAppService.toPay(1L);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrMsg()).contains("未查询到订单信息");
    }

    @Test
    @DisplayName("toPay: 订单已超时 -> Result.error")
    void toPay_orderTimeout() {
        OrderQueryVO vo = OrderQueryVO.builder()
                .timeoutTime(new Timestamp(Instant.now().minusSeconds(60).toEpochMilli()))
                .build();
        when(orderFeignClient.findById(2L)).thenReturn(Result.success(vo));

        Result<String> result = payAppService.toPay(2L);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrMsg()).contains("订单已超时");
    }

    @Test
    @DisplayName("toPay: 正常路径 -> Result.success(pay.to)")
    void toPay_success() {
        OrderQueryVO vo = OrderQueryVO.builder()
                .timeoutTime(new Timestamp(Instant.now().plusSeconds(600).toEpochMilli()))
                .payTypeName("alipay")
                .amount(new BigDecimal("19.90"))
                .createTime(new Timestamp(1_700_000_000_000L))
                .build();
        when(orderFeignClient.findById(3L)).thenReturn(Result.success(vo));
        UserHolder.setUser(UserSession.builder().id(9L).username("tester").build());

        stubGatewayEntity();
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("{\"code\":0,\"pay_info\":\"http://gateway/pay\",\"sign\":\"S\",\"sign_type\":\"RSA\"}");
        when(payService.checkSign(anyMap(), anyString())).thenReturn(true);

        Result<String> result = payAppService.toPay(3L);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("http://gateway/pay");
    }

    @Test
    @DisplayName("toPay: pay.to 为空 -> Result.error")
    void toPay_blankTo() {
        OrderQueryVO vo = OrderQueryVO.builder()
                .timeoutTime(new Timestamp(Instant.now().plusSeconds(600).toEpochMilli()))
                .payTypeName("alipay")
                .amount(new BigDecimal("19.90"))
                .createTime(new Timestamp(1_700_000_000_000L))
                .build();
        when(orderFeignClient.findById(4L)).thenReturn(Result.success(vo));
        UserHolder.setUser(UserSession.builder().id(9L).username("tester").build());

        stubGatewayEntity();
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenReturn("{\"code\":0,\"sign\":\"S\",\"sign_type\":\"RSA\"}");
        when(payService.checkSign(anyMap(), anyString())).thenReturn(true);

        Result<String> result = payAppService.toPay(4L);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrMsg()).contains("支付失败");
    }
}

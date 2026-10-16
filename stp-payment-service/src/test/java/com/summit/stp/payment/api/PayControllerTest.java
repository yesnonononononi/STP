package com.summit.stp.payment.api;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.payment.application.service.PayAppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PayController MockMvc（standaloneSetup）单元测试，不启动 Spring 容器。
 */
@ExtendWith(MockitoExtension.class)
class PayControllerTest {

    @Mock
    private PayAppService payAppService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PayController(payAppService)).build();
    }

    @Test
    @DisplayName("GET /pay/types -> 返回支付渠道列表")
    void getTypes() throws Exception {
        when(payAppService.getType()).thenReturn(Result.success(List.of("wxpay", "alipay")));

        mockMvc.perform(get("/pay/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data[0]").value("wxpay"))
                .andExpect(jsonPath("$.data[1]").value("alipay"));
    }

    @Test
    @DisplayName("GET /pay/check -> 委托 checkResult 并返回 success")
    void checkResult() throws Exception {
        mockMvc.perform(get("/pay/check")
                        .param("pid", "10001")
                        .param("out_trade_no", "123")
                        .param("trade_status", "TRADE_SUCCESS")
                        .param("sign", "S")
                        .param("sign_type", "RSA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("success"));

        verify(payAppService).checkResult(any());
    }

    @Test
    @DisplayName("POST /pay/toPay -> 返回支付去向")
    void toPay() throws Exception {
        when(payAppService.toPay(eq(1L))).thenReturn(Result.success("http://gateway/pay"));

        mockMvc.perform(post("/pay/toPay").param("orderNo", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("http://gateway/pay"));
    }
}

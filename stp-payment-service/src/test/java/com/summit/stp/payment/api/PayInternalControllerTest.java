package com.summit.stp.payment.api;

import com.summit.stp.common.application.domain.model.PayType;
import com.summit.stp.payment.api.vo.PayVO;
import com.summit.stp.payment.application.service.PayAppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PayInternalController MockMvc（standaloneSetup）单元测试。
 */
@ExtendWith(MockitoExtension.class)
class PayInternalControllerTest {

    @Mock
    private PayAppService payAppService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PayInternalController(payAppService)).build();
    }

    @Test
    @DisplayName("POST /pay/internal/submit -> 返回 PayVO")
    void submit() throws Exception {
        PayVO vo = PayVO.builder()
                .pid(10001)
                .orderId(123L)
                .type("alipay")
                .money("19.90")
                .to("http://gateway/pay")
                .build();
        when(payAppService.pay(any())).thenReturn(vo);

        String body = "{\"payType\":\"ALI_PAY\",\"amount\":19.90,\"orderId\":123,\"memberName\":\"VIP\"}";

        mockMvc.perform(post("/pay/internal/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.to").value("http://gateway/pay"))
                .andExpect(jsonPath("$.data.money").value("19.90"));
    }
}

package com.summit.stp.coupon.api;

import com.summit.stp.coupon.api.vo.CouponQueryVO;
import com.summit.stp.coupon.application.service.CouponAppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CouponTemplateController MockMvc（standaloneSetup）单元测试。
 */
@ExtendWith(MockitoExtension.class)
class CouponTemplateControllerTest {

    @Mock
    private CouponAppService couponAppService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CouponTemplateController(couponAppService)).build();
    }

    @Test
    @DisplayName("GET /coupon/template/{id} -> 返回优惠券模板")
    void getTemplate() throws Exception {
        when(couponAppService.queryById(1L))
                .thenReturn(CouponQueryVO.builder().id(1L).name("满减券").status(1).build());

        mockMvc.perform(get("/coupon/template/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.name").value("满减券"));
    }
}

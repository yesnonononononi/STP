package com.summit.stp.common.application.domain.model;

import com.summit.stp.common.application.domain.exception.ParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * PayType（跨服务共享枚举）单元测试。
 */
class PayTypeTest {

    @Test
    @DisplayName("fromCode: null -> null")
    void fromCode_null() {
        assertThat(PayType.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode: 1/2 正常映射")
    void fromCode_known() {
        assertThat(PayType.fromCode(1)).isEqualTo(PayType.WX_PAY);
        assertThat(PayType.fromCode(2)).isEqualTo(PayType.ALI_PAY);
    }

    @Test
    @DisplayName("fromCode: 未知编码 -> ParameterException")
    void fromCode_unknown() {
        assertThatThrownBy(() -> PayType.fromCode(99))
                .isInstanceOf(ParameterException.class);
    }

    @Test
    @DisplayName("fromType: null/空串 -> null")
    void fromType_blank() {
        assertThat(PayType.fromType(null)).isNull();
        assertThat(PayType.fromType("")).isNull();
    }

    @Test
    @DisplayName("fromType: 名称与编码均可识别(忽略大小写)")
    void fromType_known() {
        assertThat(PayType.fromType("alipay")).isEqualTo(PayType.ALI_PAY);
        assertThat(PayType.fromType("ALIPAY")).isEqualTo(PayType.ALI_PAY);
        assertThat(PayType.fromType("wxpay")).isEqualTo(PayType.WX_PAY);
        assertThat(PayType.fromType("1")).isEqualTo(PayType.WX_PAY);
        assertThat(PayType.fromType("2")).isEqualTo(PayType.ALI_PAY);
    }

    @Test
    @DisplayName("fromType: 未知类型 -> ParameterException")
    void fromType_unknown() {
        assertThatThrownBy(() -> PayType.fromType("paypal"))
                .isInstanceOf(ParameterException.class);
    }
}

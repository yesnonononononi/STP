package com.summit.stp.order.order.domain.model;

import com.summit.stp.common.application.domain.exception.ParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OrderStatus 枚举单元测试。
 */
class OrderStatusTest {

    @Test
    @DisplayName("fromCode: null -> PENDING")
    void fromCode_null() {
        assertThat(OrderStatus.fromCode(null)).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("fromCode: 0/1/2/3 正常映射")
    void fromCode_known() {
        assertThat(OrderStatus.fromCode(0)).isEqualTo(OrderStatus.PENDING);
        assertThat(OrderStatus.fromCode(1)).isEqualTo(OrderStatus.PAID);
        assertThat(OrderStatus.fromCode(2)).isEqualTo(OrderStatus.COMPLETED);
        assertThat(OrderStatus.fromCode(3)).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("fromCode: 未知编码 -> ParameterException")
    void fromCode_unknown() {
        assertThatThrownBy(() -> OrderStatus.fromCode(99))
                .isInstanceOf(ParameterException.class);
    }
}

package com.summit.stp.order.order.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Order 聚合根状态机单元测试。
 */
class OrderTest {

    private Order orderWith(OrderStatus status) {
        return Order.builder()
                .id(1000L)
                .creatorId(7L)
                .status(status)
                .build();
    }

    @Test
    @DisplayName("PENDING -> paid() -> PAID 并记录时间")
    void paid_fromPending() {
        Order order = orderWith(OrderStatus.PENDING);

        order.paid();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPayTime()).isNotNull();
        assertThat(order.getUpdateTime()).isNotNull();
    }

    @Test
    @DisplayName("PAID -> complete() -> COMPLETED")
    void complete_fromPaid() {
        Order order = orderWith(OrderStatus.PAID);

        order.complete();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getUpdateTime()).isNotNull();
    }

    @Test
    @DisplayName("COMPLETED -> complete() 不改变状态")
    void complete_notPaid_Noop() {
        Order order = orderWith(OrderStatus.PENDING);

        order.complete();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("PAID 再 paid() 幂等")
    void paid_whenPaid_idempotent() {
        Order order = orderWith(OrderStatus.PAID);

        order.paid();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("COMPLETED 再 paid() 幂等")
    void paid_whenCompleted_idempotent() {
        Order order = orderWith(OrderStatus.COMPLETED);

        order.paid();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("CANCELLED 可 paid() -> PAID")
    void paid_whenCancelled() {
        Order order = orderWith(OrderStatus.CANCELLED);

        order.paid();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("非 PENDING cancel() -> IllegalStateException")
    void cancel_nonPending_throws() {
        assertThatThrownBy(() -> orderWith(OrderStatus.PAID).cancel())
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> orderWith(OrderStatus.COMPLETED).cancel())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("PENDING cancel() -> CANCELLED")
    void cancel_fromPending() {
        Order order = orderWith(OrderStatus.PENDING);

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("of(uid) 权限判断")
    void of() {
        Order order = orderWith(OrderStatus.PENDING);

        assertThat(order.of(7L)).isTrue();
        assertThat(order.of(8L)).isFalse();
    }
}

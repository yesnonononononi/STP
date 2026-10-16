package com.summit.stp.order.admin.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.model.PayType;
import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.order.admin.application.command.AdminQueryCommand;
import com.summit.stp.order.admin.application.vo.AdminOrderVO;
import com.summit.stp.order.order.application.service.OrderReconciliationAppService;
import com.summit.stp.order.order.domain.model.Order;
import com.summit.stp.order.order.domain.model.OrderStatus;
import com.summit.stp.order.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AdminOrderServiceImpl 纯单元测试。
 */
@ExtendWith(MockitoExtension.class)
class AdminOrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderReconciliationAppService orderReconciliationAppService;
    @Mock
    private DistributedLockUtil distributedLockUtil;

    private AdminOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminOrderServiceImpl(orderRepository, orderReconciliationAppService, distributedLockUtil);
        // 让分布式锁工具直接执行任务（Supplier / Runnable 两个重载都可用）
        lenient().doAnswer(inv -> ((Supplier<?>) inv.getArgument(3)).get())
                .when(distributedLockUtil).executeWithLock(anyString(), anyLong(), anyLong(), any(Supplier.class));
        lenient().doAnswer(inv -> {
            ((Runnable) inv.getArgument(3)).run();
            return null;
        }).when(distributedLockUtil).executeWithLock(anyString(), anyLong(), anyLong(), any(Runnable.class));
    }

    @Test
    @DisplayName("listBy: 分页映射为 AdminOrderVO 并脱敏账户")
    void listBy() {
        Order order = Order.builder()
                .id(1L).creatorId(7L).amount(new BigDecimal("19.90"))
                .payType(PayType.ALI_PAY).to("1234567890")
                .status(OrderStatus.PAID).packageId(2001L).quantity(1)
                .build();
        Page<Order> page = new Page<>(1, 10);
        page.setRecords(List.of(order));
        page.setTotal(1);
        when(orderRepository.findPage(any())).thenReturn(page);

        Result<PageResult<List<AdminOrderVO>>> result = service.listBy(AdminQueryCommand.builder().build());

        assertThat(result.isSuccess()).isTrue();
        List<AdminOrderVO> data = result.getData().getData();
        assertThat(data).hasSize(1);
        AdminOrderVO vo = data.get(0);
        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getPayType()).isEqualTo(2);
        assertThat(vo.getStatus()).isEqualTo(1);
        assertThat(vo.getTo()).isEqualTo("123****7890");
    }

    @Test
    @DisplayName("reissuance: orderNo 为 null -> Result.error")
    void reissuance_null() {
        Result<Void> result = service.reissuance(null);

        assertThat(result.isSuccess()).isFalse();
        verify(orderReconciliationAppService, never()).compensateOrder(anyLong());
    }

    @Test
    @DisplayName("reissuance: 正常 -> 加锁执行补单")
    void reissuance_success() {
        when(orderReconciliationAppService.compensateOrder(1000L)).thenReturn(Result.success());

        Result<Void> result = service.reissuance(1000L);

        assertThat(result.isSuccess()).isTrue();
        verify(orderReconciliationAppService).compensateOrder(1000L);
    }

    @Test
    @DisplayName("verifyStatus: orderNo 为 null -> Result.error")
    void verifyStatus_null() {
        Result<String> result = service.verifyStatus(null);

        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("verifyStatus: 正常 -> 委托 reconcileOrder(requireCompensation=false)")
    void verifyStatus_success() {
        when(orderReconciliationAppService.reconcileOrder(1000L, false)).thenReturn(Result.success("ok"));

        Result<String> result = service.verifyStatus(1000L);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("refund: orderNo 为 null -> Result.error")
    void refund_null() {
        Result<Void> result = service.refund(null, "r");

        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("refund: 未实现 -> 抛 BusinessException（禁止静默假成功）")
    void refund_notImplemented() {
        assertThatThrownBy(() -> service.refund(1000L, "r"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("退款功能暂未开放");
    }
}

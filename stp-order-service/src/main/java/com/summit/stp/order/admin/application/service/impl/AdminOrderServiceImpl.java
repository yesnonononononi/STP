package com.summit.stp.order.admin.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.order.admin.application.command.AdminQueryCommand;
import com.summit.stp.order.admin.application.service.AdminOrderService;
import com.summit.stp.order.admin.application.vo.AdminOrderVO;
import com.summit.stp.order.order.application.service.OrderReconciliationAppService;
import com.summit.stp.order.order.domain.model.Order;
import com.summit.stp.order.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {
    private final OrderRepository orderRepository;
    private final OrderReconciliationAppService orderReconciliationAppService;

    @Override
    public Result<PageResult<List<AdminOrderVO>>> listBy(AdminQueryCommand command) {
        Page<Order> p = orderRepository.findPage(command);
        return Result.success(new PageResult<>(p.getCurrent(), p.getTotal(), p.getRecords().stream().map(this::toVO).toList()));
    }

    @Override
    public Result<Void> reissuance(Long orderNo) {
        if (orderNo == null) return Result.error("订单编号不能为空");
        return orderReconciliationAppService.compensateOrder(orderNo);
    }

    @Override
    public Result<Void> refund(Long orderNo, String reason) {
        return null;
    }

    @Override
    public Result<String> verifyStatus(Long orderNo) {
        if (orderNo == null) return Result.error("订单编号不能为空");
        return orderReconciliationAppService.reconcileOrder(orderNo, false);
    }

    private AdminOrderVO toVO(Order order) {
        return AdminOrderVO.builder()
                .id(order.getId())
                .creatorId(order.getCreatorId())
                .amount(order.getAmount())
                .payType(order.getPayType().getCode())
                .to(order.getTo())
                .sign(order.getSign())
                .status(order.getStatus().getCode())
                .packageId(order.getPackageId())
                .quantity(order.getQuantity())
                .couponId(order.getCouponId())
                .unitPrice(order.getUnitPrice())
                .discountAmount(order.getDiscountAmount())
                .createTime(order.getCreateTime())
                .timeoutTime(order.getTimeoutTime())
                .updateTime(order.getUpdateTime())
                .payTime(order.getPayTime())
                .build();
    }
}

package com.summit.stp.order.admin.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.util.DistributedLockUtil;
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

    /** 管理端写操作分布式锁等待时间（秒） */
    private static final long LOCK_WAIT_SECONDS = 3L;
    /** 管理端写操作分布式锁租约时间（秒） */
    private static final long LOCK_LEASE_SECONDS = 10L;

    private final OrderRepository orderRepository;
    private final OrderReconciliationAppService orderReconciliationAppService;
    private final DistributedLockUtil distributedLockUtil;

    @Override
    public Result<PageResult<List<AdminOrderVO>>> listBy(AdminQueryCommand command) {
        Page<Order> p = orderRepository.findPage(command);
        return Result.success(new PageResult<>(p.getCurrent(), p.getTotal(), p.getRecords().stream().map(this::toVO).toList()));
    }

    @Override
    public Result<Void> reissuance(Long orderNo) {
        if (orderNo == null) return Result.error("订单编号不能为空");
        // 同一订单的补单请求串行化，避免并发重复补偿（幂等/防重，#24）
        return distributedLockUtil.executeWithLock(
                "admin:order:reissuance:" + orderNo, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS,
                () -> orderReconciliationAppService.compensateOrder(orderNo));
    }

    /**
     * 退款（暂未开放）。
     *
     * <p><b>TODO(退款链路接入点)</b>：当前管理端尚未接入真实退款编排，禁止返回 {@code null}
     * 造成「HTTP 200 空响应」的静默假成功（历史缺陷 #7）。后续接入点：
     * <ul>
     *     <li>payment-service 的 {@code RefundAppService}：按 orderNo 发起三方退款编排；</li>
     *     <li>落退款单并回写订单状态；</li>
     *     <li>接入后沿用本方法已就位的分布式锁 key（{@code admin:order:refund:{orderNo}}）保证幂等。</li>
     * </ul>
     */
    @Override
    public Result<Void> refund(Long orderNo, String reason) {
        if (orderNo == null) return Result.error("订单编号不能为空");
        // 同一订单的退款请求串行化（幂等/防重，接入真实链路后直接复用，避免重复退款）
        return distributedLockUtil.executeWithLock(
                "admin:order:refund:" + orderNo, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS,
                this::refundNotImplemented);
    }

    /**
     * 退款未实现占位：显式抛出业务异常，禁止返回 null 造成「HTTP 200 空响应」的静默假成功（历史缺陷 #7）。
     */
    private Result<Void> refundNotImplemented() {
        throw new BusinessException("退款功能暂未开放");
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
                .to(maskAccount(order.getTo()))
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

    /**
     * 收款账户/地址脱敏：长度 &gt; 8 保留前 3 后 4，4 &lt; 长度 &le; 8 保留首尾各 1 位，其余全遮罩（#21）。
     */
    private static String maskAccount(String account) {
        if (account == null || account.isBlank()) return account;
        if (account.contains("*")) return account;
        int len = account.length();
        if (len <= 4) return "****";
        if (len <= 8) return account.charAt(0) + "****" + account.charAt(len - 1);
        return account.substring(0, 3) + "****" + account.substring(len - 4);
    }
}

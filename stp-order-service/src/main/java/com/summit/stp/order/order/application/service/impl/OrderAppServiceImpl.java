package com.summit.stp.order.order.application.service.impl;

import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.order.order.application.vo.OrderCreateVO;
import com.summit.stp.order.order.domain.exception.OrderNotFoundException;
import com.summit.stp.payment.api.dto.PayCommand;
import com.summit.stp.common.application.domain.event.OrderPaidEvent;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.application.domain.model.PayType;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.coupon.api.vo.CouponQueryVO;
import com.summit.stp.user.api.vo.MemberVO;
import com.summit.stp.order.api.vo.OrderQueryVO;
import com.summit.stp.coupon.api.client.CouponFeignClient;
import com.summit.stp.user.api.client.MemberFeignClient;
import com.summit.stp.payment.api.client.PayFeignClient;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.order.order.api.dto.OrderCreateRequest;
import com.summit.stp.order.order.application.service.OrderAppService;
import com.summit.stp.order.order.application.service.OrderReconciliationAppService;
import com.summit.stp.order.order.application.service.OrderTimeoutProvider;
import com.summit.stp.order.order.domain.model.Order;
import com.summit.stp.order.order.domain.model.OrderStatus;
import com.summit.stp.order.order.domain.repository.OrderRepository;
import com.summit.stp.order.order.infrastructure.constants.OrderConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;

/**
 * 订单应用服务实现类
 * 直接依赖 OrderRepository 进行编排，彻底砍掉无业务逻辑的冗余领域服务 OrderService (符合 DDD)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAppServiceImpl implements OrderAppService {
    private final OrderRepository orderRepository;
    private final CouponFeignClient couponFeignClient;
    private final PayFeignClient payFeignClient;
    private final MemberFeignClient memberFeignClient;
    private final OrderTimeoutProvider orderTimeoutProvider;
    private final EventPublishProvider eventPublishProvider;
    private final OrderReconciliationAppService orderReconciliationAppService;

    @Override
    public OrderQueryVO queryById(Long orderId) {
        Order order = orderRepository.findOrderById(orderId);
        if (order == null) {
            return null;
        }
        // 查询订单商品 (RPC)
        MemberVO memberVO = memberFeignClient.queryMemberById(order.getPackageId()).getData();

        // 查询订单所使用的优惠券信息 (RPC)
        CouponQueryVO couponQueryVO = order.getCouponId() == null ? null
                : couponFeignClient.queryById(order.getCouponId()).getData();

        return mapToQueryResponse(order, memberVO, couponQueryVO);
    }

    @Override
    public void deleteById(Long orderId) {
        Long uid = UserHolder.getUser().getId();
        Order orderById = orderRepository.findOrderById(orderId);
        if (orderById.of(uid)) {
            orderRepository.deleteById(orderId);
        }
        else {
            throw new ParameterException("无权限删除");
        }
    }

    @Override
    public List<OrderQueryVO> queryHistory(long page, long pageSize, Integer status) {
        List<Order> orders = orderRepository.queryHistoryOrders(page, pageSize, status);
        List<Long> list = orders.stream().map(Order::getPackageId).distinct().toList();
        List<Long> cList = orders.stream().map(Order::getCouponId).filter(Objects::nonNull).toList();

        Map<Long, MemberVO> mapResult = memberFeignClient.queryMemberByIds(list).getData();
        Map<Long, CouponQueryVO> mapCResult = cList.isEmpty() ? Collections.emptyMap()
                : couponFeignClient.queryByIds(cList).getData();

        return orders.stream()
                .map(order -> mapToQueryResponse(
                        order,
                        mapResult == null ? null : (order.getPackageId() == null ? null : mapResult.get(order.getPackageId())),
                        mapCResult == null ? null : (order.getCouponId() == null ? null : mapCResult.get(order.getCouponId()))
                )).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ackOrder(Long orderId) {
        // 1. [应用服务层] 校验订单是否存在
        Order order = orderRepository.findOrderById(orderId);
        if (order == null) {
            throw new ParameterException("订单不存在，ID: " + orderId);
        }
        // 2. 领域模型行为：标记为已支付
        orderRepository.ackOrderIfWaitPay(orderId);

        // 3.1 移除 Redis 超时检测记录
        orderTimeoutProvider.cancelTimeout(orderId);

        // 4. 发布订单支付事件
        eventPublishProvider.publish(
                OrderPaidEvent.builder()
                        .payTime(order.getUpdateTime())
                        .quantity(order.getQuantity())
                        .packageId(order.getPackageId())
                        .orderId(order.getId())
                        .creatorId(order.getCreatorId())
                        .build()
        );
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Result<OrderCreateVO> createOrder(OrderCreateRequest orderCreateRequest) {
        // 强制购买数量为 1
        orderCreateRequest.setQuantity(1);
        UserSession user = UserHolder.getUser();

        // 获取商品信息
        final MemberVO member = memberFeignClient.queryMemberById(orderCreateRequest.getPackageId()).getData();
        final Long orderId = orderRepository.generateOrderId();
        final Timestamp now = new Timestamp(System.currentTimeMillis());
        final Timestamp timeoutTime = new Timestamp(now.getTime() + OrderConstants.Business.TIMEOUT_MILLIS);
        final Long couponId = orderCreateRequest.getCouponId();
        final BigDecimal memberDiscount = member.getDiscount() == null  ? null : new BigDecimal(member.getDiscount());
        final BigDecimal price = member.getPrice();
        final BigDecimal memberDiscountedPrice = Optional.ofNullable(memberDiscount)
                .map(price::multiply)
                .orElse(price);


        //使用优惠券
        if (couponId != null) {
                couponFeignClient.use(couponId, orderId,member.getTypeId(),member.getId());
        }

        //计算总共优惠券 + 套餐折扣减免的总金额
        BigDecimal amount = Optional.ofNullable(couponId)
                .map(id -> couponFeignClient.calculateAmount(memberDiscountedPrice, orderCreateRequest.getQuantity(), id).getData())
                .orElseGet(() -> memberDiscountedPrice.multiply(BigDecimal.valueOf(orderCreateRequest.getQuantity())).setScale(2, RoundingMode.HALF_UP));

        try {

            PayType payType = PayType.fromCode(orderCreateRequest.getPayType());
            //计算需要支付的金额
            BigDecimal discountAmount = price.multiply(BigDecimal.valueOf(orderCreateRequest.getQuantity())).subtract(amount);

            //创建订单并保存
            Order order = buildOrder(orderId, payType, amount, user, orderCreateRequest, price, discountAmount, now, timeoutTime);
            orderRepository.save(order);

            // 事务提交后注册订单超时，Redis 不可用不影响订单落库
            orderTimeoutProvider.registerTimeout(timeoutTime, orderId);

            return Result.success(
                    OrderCreateVO.builder()
                            .orderId(orderId)
                            .endTime(timeoutTime)
                            .build()
            );

        } catch (Exception e) {
            log.error("【订单】订单创建失败，进行补偿回滚，订单ID: {}", orderId, e);
            throw e;
        }
    }

    @Override
    @GlobalTransactional(
            rollbackFor = Exception.class
    )
    public void cancelOrderTimeout(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        List<Order> changedOrders = new ArrayList<>();
        for (Order order : orders) {
            Long orderId = order.getId();
            if (order.getStatus() != OrderStatus.PENDING) {
                log.info("【超时取消】订单状态不是待支付，忽略取消订单操作，订单ID: {}", orderId);
                continue;
            }

            try {
                Result<String> reconcileResult = orderReconciliationAppService.reconcileOrder(orderId,true);
                if (reconcileResult.isSuccess()) {
                    log.warn("【超时取消】检测到该订单在三方已支付，已成功触发补单。取消关单逻辑，订单ID: {}", orderId);
                    continue;
                }
            } catch (Exception e) {
                log.error("【超时取消】前置对账异常，订单ID: {}", orderId, e);
            }

            // 订单状态变更必须通过领域动作完成，仓储层只负责批量持久化。
            order.cancel();
            changedOrders.add(order);
        }

        if (changedOrders.isEmpty()) {
            return;
        }

        orderRepository.batchUpdate(changedOrders);
        for (Order order : changedOrders) {
            Long couponId = order.getCouponId();
            if (couponId == null) {
                continue;
            }
            try {
                couponFeignClient.refund(couponId);
                log.info("【超时取消】订单优惠券已退回，ID: {}, 优惠券ID: {}", order.getId(), couponId);
            } catch (Exception e) {
                log.error("【超时取消】订单优惠券退回异常，ID: {}, 优惠券ID: {}", order.getId(), couponId, e);
                throw e;
            }
        }
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findOrderById(orderId);
        if (order == null) {
            log.warn("【订单取消】订单不存在，ID: {}", orderId);
            return;
        }
        order.cancel();
        orderRepository.updateById(order);
        orderTimeoutProvider.cancelTimeout(orderId);
        if (order.getCouponId() != null) {
            try {
                couponFeignClient.refund(order.getCouponId());
                log.info("【订单取消】订单优惠券已成功退回，ID: {}, 优惠券ID: {}", orderId, order.getCouponId());
            } catch (Exception e) {
                log.error("【订单取消】订单优惠券退回异常，ID: {}, 优惠券ID: {}", orderId, order.getCouponId(), e);
                throw e;
            }
        }
    }

    @Override
    public Result<Void> ack(Long orderNo) {
        Order order = orderRepository.findById(orderNo).orElseThrow(OrderNotFoundException::new);
        order.complete();
        orderRepository.updateById(order);
        return Result.success();
    }

    private OrderQueryVO mapToQueryResponse(Order order, MemberVO memberVO, CouponQueryVO couponQueryVO) {
        BigDecimal payableAmount = order.getUnitPrice() != null && order.getQuantity() != null
                ? order.getUnitPrice().multiply(BigDecimal.valueOf(order.getQuantity()))
                : order.getAmount();

        return OrderQueryVO.builder()
                .orderId(order.getId())
                .amount(order.getAmount())
                .status(String.valueOf(order.getStatus().getCode()))
                .payTypeName(order.getPayType() != null ? String.valueOf(PayType.fromType(order.getPayType().getType()).getCode()) : "未知")
                .createTime(order.getCreateTime())
                .timeoutTime(order.getTimeoutTime())
                .toName(order.getTo())
                .couponName(couponQueryVO == null ? "未使用优惠券" : couponQueryVO.getName())
                .couponId(couponQueryVO == null ? null : couponQueryVO.getId())
                .memberId(memberVO == null ? null : memberVO.getId())
                .memberName(memberVO == null ? "未查询到套餐信息" : memberVO.getName())
                .payableAmount(payableAmount)
                .payTime(order.getPayTime())
                .build();
    }


    private Order buildOrder(Long id, PayType payType, BigDecimal amount, UserSession user, OrderCreateRequest orderCreateRequest, BigDecimal unitPrice, BigDecimal discountAmount, Timestamp timestamp, Timestamp timeoutTime) {
        return Order.builder()
            .id(id)
            .payType(payType)
            .amount(amount)
            .creatorId(user.getId())
            .packageId(orderCreateRequest.getPackageId())
            .quantity(orderCreateRequest.getQuantity())
            .couponId(orderCreateRequest.getCouponId())
            .unitPrice(unitPrice)
            .discountAmount(discountAmount)
            .createTime(timestamp)
            .timeoutTime(timeoutTime)
            .updateTime(timestamp)
            .status(OrderStatus.PENDING)
            .build();
    }

}


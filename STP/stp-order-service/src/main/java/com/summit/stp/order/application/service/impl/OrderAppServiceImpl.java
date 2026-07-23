package com.summit.stp.order.application.service.impl;

import com.summit.stp.shared.application.vo.CouponQueryVO;
import com.summit.stp.shared.application.vo.MemberVO;
import com.summit.stp.order.api.dto.OrderCreateRequest;
import com.summit.stp.order.application.service.*;
import com.summit.stp.shared.application.vo.OrderQueryVO;
import com.summit.stp.order.application.vo.ProductVO;
import com.summit.stp.shared.domain.event.OrderPaidEvent;
import com.summit.stp.order.domain.model.Order;
import com.summit.stp.order.domain.model.OrderStatus;
import com.summit.stp.order.domain.repository.OrderRepository;
import com.summit.stp.shared.application.command.PayCommand;
import com.summit.stp.shared.application.vo.PayVO;
import com.summit.stp.shared.domain.model.PayType;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.order.infrastructure.constants.OrderConstants;
import com.summit.stp.shared.exception.ParameterException;
import com.summit.stp.shared.result.Result;
import com.summit.stp.userAuth.domain.model.UserSession;
import com.summit.stp.common.feign.CouponFeignClient;
import com.summit.stp.common.feign.PayFeignClient;
import com.summit.stp.common.feign.MemberFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    private final ProductProvider productProvider;
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
        orderRepository.deleteById(orderId);
    }

    @Override
    public List<OrderQueryVO> queryHistory(long page, long pageSize) {
        List<Order> orders = orderRepository.queryHistoryOrders(page, pageSize);
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
        // 2. [领域实体层] 校验订单是否已完成，并进行合法状态转移
        order.payComplete();

        // 3. [应用服务层协调] 将更新后的领域聚合根持久化存回数据库
        orderRepository.save(order);

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
    @Transactional(rollbackFor = Exception.class)
    public Result<PayVO> createOrder(OrderCreateRequest orderCreateRequest) {
        // 强制购买数量为 1
        orderCreateRequest.setQuantity(1);
        UserSession user = UserHolder.getUser();
        
        // 获取商品信息
        ProductVO product;
        try {
            product = productProvider.getProductInfo(orderCreateRequest.getPackageId());
        } catch (ParameterException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("【订单】获取商品信息系统异常, packageId: {}", orderCreateRequest.getPackageId(), e);
            throw e;
        }

        Long orderId = orderRepository.generateOrderId();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Long couponId = orderCreateRequest.getCouponId();

        // 前置强校验：优惠券适用范围校验
        if (couponId != null) {
            try {
                MemberVO member = memberFeignClient.queryMemberById(orderCreateRequest.getPackageId()).getData();
                if (member == null) {
                    return Result.error("未找到会员套餐信息");
                }
                couponFeignClient.validateCouponApplicability(couponId, member.getTypeId(), member.getId());
            } catch (ParameterException e) {
                return Result.error(e.getMessage());
            }
        }

        BigDecimal discountedPrice = product.getPrice();
        if (product.getDiscount() != null) {
            discountedPrice = discountedPrice.multiply(product.getDiscount());
        }
        BigDecimal amount;
        if (couponId == null) {
            amount = discountedPrice.multiply(BigDecimal.valueOf(orderCreateRequest.getQuantity())).setScale(2, RoundingMode.HALF_UP);
        } else {
            amount = couponFeignClient.calculateAmount(discountedPrice, orderCreateRequest.getQuantity(), couponId).getData();
        }

        // Saga 手动事务补偿变量
        boolean couponUsed = false;
        try {
            // 1. 远程调用优惠券微服务扣减优惠券
            if (couponId != null) {
                couponFeignClient.use(couponId, orderId);
                couponUsed = true;
            }

            // 2. 本地写数据库保存订单
            PayType payType = PayType.fromCode(orderCreateRequest.getPayType());

            //2.1计算优惠金额
            BigDecimal unitPrice = product.getPrice();
            BigDecimal discountAmount = unitPrice.multiply(BigDecimal.valueOf(orderCreateRequest.getQuantity())).subtract(amount);

            orderRepository.save(
                    Order.builder()
                            .id(orderId)
                            .payType(payType)
                            .amount(amount)
                            .creatorId(user.getId())
                            .packageId(orderCreateRequest.getPackageId())
                            .quantity(orderCreateRequest.getQuantity())
                            .couponId(orderCreateRequest.getCouponId())
                            .unitPrice(unitPrice)
                            .discountAmount(discountAmount)
                            .createTime(timestamp)
                            .updateTime(timestamp)
                            .status(OrderStatus.PENDING)
                            .build()
            );

            // 3. 注册订单超时
            orderTimeoutProvider.registerTimeout(OrderConstants.Business.TIMEOUT, orderId);

            // 4. 远程调用支付微服务发起支付
            PayCommand payCommand = PayCommand.builder()
                    .username(user.getUsername())
                    .amount(amount)
                    .orderId(orderId)
                    .payType(payType)
                    .memberName(product.getName())
                    .timestamp(timestamp)
                    .build();
            log.info("【订单】发起支付 RPC 调用，参数: {}", payCommand);
            Result<PayVO> payResult = payFeignClient.pay(payCommand);
            log.info("【订单】支付 RPC 调用返回，结果: {}", payResult);
            PayVO pay = payResult != null ? payResult.getData() : null;

            return Result.success(pay);

        } catch (Exception e) {
            log.error("【订单】订单创建失败，进行补偿回滚，订单ID: {}", orderId, e);
            // 失败时反向补偿操作回滚优惠券
            if (couponUsed) {
                try {
                    couponFeignClient.refund(couponId);
                    log.info("【订单】Saga 手动补偿成功：退回优惠券ID: {}", couponId);
                } catch (Exception ex) {
                    log.error("【订单】Saga 手动补偿异常：退回优惠券失败，优惠券ID: {}", couponId, ex);
                }
            }
            throw e; // 抛出异常，触发本地数据库订单状态的回滚
        }
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            rollbackFor = Exception.class
    )
    public void cancelOrderTimeout(Set<String> orders) {
        List<Order> changedOrders = new ArrayList<>();
        Map<Long, Order> order = orderRepository.findOrderByIds(orders);

        order.forEach((key, orderEntity) -> {
            Long orderId = orderEntity.getId();
            if (orderEntity.getStatus() != OrderStatus.PENDING) {
                log.info("【超时取消】订单状态不是待支付，忽略取消订单操作，订单ID: {}", orderId);
                return;
            }
            changedOrders.add(orderEntity);
            try {
                Result<String> reconcileResult = orderReconciliationAppService.reconcileOrder(orderId);
                if (reconcileResult.isSuccess()) {
                    log.warn("【超时取消】检测到该订单在三方已支付，已成功触发补单。取消关单逻辑，订单ID: {}", orderId);
                    return;
                }
            } catch (Exception e) {
                log.error("【超时取消】前置对账异常，订单ID: {}", orderId, e);
            }
            orderEntity.cancel();
            orderTimeoutProvider.cancelTimeout(orderId);
            
            Long couponId = orderEntity.getCouponId();
            if (couponId != null) {
                try {
                    couponFeignClient.refund(couponId);
                    log.info("【超时取消】订单优惠券已退回，ID: {}, 优惠券ID: {}", orderId, couponId);
                } catch (Exception e) {
                    log.error("【超时取消】订单优惠券退回异常，ID: {}, 优惠券ID: {}", orderId, couponId, e);
                    throw e;
                }
            }
        });

        orderRepository.batchUpdate(changedOrders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findOrderById(orderId);
        if (order == null) {
            log.warn("【订单取消】订单不存在，ID: {}", orderId);
            return;
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            log.info("【订单取消】订单状态已变更，忽略取消，ID: {}, 当前状态: {}", orderId, order.getStatus());
            return;
        }

        order.cancel();
        orderRepository.save(order);
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
                .toName(order.getTo())
                .couponName(couponQueryVO == null ? "未使用优惠券" : couponQueryVO.getName())
                .couponId(couponQueryVO == null ? null : couponQueryVO.getId())
                .memberId(memberVO == null ? null : memberVO.getId())
                .memberName(memberVO == null ? "未查询到套餐信息" : memberVO.getName())
                .payableAmount(payableAmount)
                .payTime(order.getPayTime())
                .build();
    }
}

package com.summit.stp.order.application.service.impl;

import com.summit.stp.coupon.application.service.CouponAppService;
import com.summit.stp.coupon.application.vo.CouponQueryVO;
import com.summit.stp.member.application.service.MemberAppService;
import com.summit.stp.member.application.vo.MemberVO;
import com.summit.stp.order.api.dto.OrderCreateRequest;
import com.summit.stp.order.application.service.OrderAppService;
import com.summit.stp.order.application.service.OrderReconciliationAppService;
import com.summit.stp.order.application.service.OrderTimeoutProvider;
import com.summit.stp.order.application.service.ProductProvider;
import com.summit.stp.order.application.vo.OrderQueryVO;
import com.summit.stp.order.application.vo.ProductVO;
import com.summit.stp.order.domain.event.OrderPaidEvent;
import com.summit.stp.order.domain.model.Order;
import com.summit.stp.order.domain.model.OrderStatus;
import com.summit.stp.order.domain.repository.OrderRepository;
import com.summit.stp.order.domain.service.OrderDomainService;
import com.summit.stp.payment.application.command.PayCommand;
import com.summit.stp.payment.application.command.RefundCommand;
import com.summit.stp.payment.application.service.PayAppService;
import com.summit.stp.payment.application.service.RefundAppService;
import com.summit.stp.payment.application.vo.PayVO;
import com.summit.stp.payment.application.vo.RefundResultVO;
import com.summit.stp.payment.domain.model.PayType;
import com.summit.stp.order.application.service.EventPublishProvider;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.result.Result;
import com.summit.stp.shared.exception.ParameterException;
import com.summit.stp.userAuth.domain.model.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 订单应用服务实现类
 * 直接依赖 OrderRepository 进行编排，彻底砍掉无业务逻辑的冗余领域服务 OrderService (符合 DDD)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAppServiceImpl implements OrderAppService {
    private final OrderRepository orderRepository;
    private final CouponAppService couponAppService;
    private final PayAppService payAppService;
    private final RefundAppService refundAppService;
    private final OrderDomainService orderDomainService;
    private final OrderTimeoutProvider orderTimeoutProvider;
    private final ProductProvider productProvider;
    private final ApplicationContext applicationContext;
    private final MemberAppService memberAppService;
    private final EventPublishProvider eventPublishProvider;
    private final OrderReconciliationAppService orderReconciliationAppService;
    private OrderAppService self;

    private OrderAppService getSelf() {
        if (self == null) {
            self = applicationContext.getBean(OrderAppService.class);
        }
        return self;
    }

    private final long ORDER_TIMEOUT = 60 * 1000;


    @Override
    public OrderQueryVO queryById(Long orderId) {
        Order order = orderRepository.findOrderById(orderId);
        if (order == null) {
            return null;
        }
        //查询订单商品
        MemberVO memberVO = memberAppService.queryMemberById(order.getPackageId()).getData();

        //查询订单所使用的优惠券信息
        CouponQueryVO couponQueryVO = couponAppService.queryById(order.getCouponId());


        return mapToQueryResponse(order, memberVO, couponQueryVO);
    }

    @Override
    public void deleteById(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    @Override
    public List<OrderQueryVO> queryHistory(long page, long pageSize) {
        List<Order> orders = orderRepository.queryHistoryOrders(page, pageSize);
        return orders.stream()
                .map(order -> mapToQueryResponse(order, null, null)).toList();
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

        //4,发布订单支付事件
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
        // 强制购买数量为 1，不能由前端决定
        orderCreateRequest.setQuantity(1);
        UserSession user = UserHolder.getUser();
        //获取商品信息（这里为会员套餐）
        ProductVO product;
        try {
            product = productProvider.getProductInfo(orderCreateRequest.getPackageId());
        } catch (ParameterException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取商品信息系统异常, packageId: {}", orderCreateRequest.getPackageId(), e);
            throw e;
        }

        //生成订单id和时间戳
        Long orderId = orderRepository.generateOrderId();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        //优惠后的金额
        Long couponId = orderCreateRequest.getCouponId();
        BigDecimal amount = couponAppService.calculateAmount(product.getPrice(), orderCreateRequest.getQuantity(), couponId);



        //原子扣减用户
        couponAppService.use(couponId, orderId);


        //创建并保存订单
        PayType payType = PayType.fromCode(orderCreateRequest.getPayType());
        BigDecimal unitPrice = product.getPrice();
        BigDecimal discountAmount = unitPrice.multiply(BigDecimal.valueOf(orderCreateRequest.getQuantity())).subtract(amount);

        //(1) 先写数据库
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


        //(2)  订单超时
        orderTimeoutProvider.registerTimeout(ORDER_TIMEOUT,orderId);



        //调用支付领域驱动服务
        PayVO pay = payAppService.pay(
                PayCommand.builder()
                        .username(user.getUsername())
                        .amount(amount)
                        .orderId(orderId)
                        .payType(payType)
                        .memberName(product.getName())
                        .timestamp(timestamp)
                        .build()
        );

        return Result.success(pay);
    }

    @Override
    public void timeoutOrder(Set<String> orders) {
        for (String order : orders) {
            try {
                getSelf().cancelOrderTimeout(Long.valueOf(order));
            } catch (Exception e) {
                log.error("订单超时处理异常: {}", order, e);
            }
        }
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            rollbackFor = Exception.class
    )
    public void cancelOrderTimeout(Long orderId) {
        // 1. 查找订单
        Order order = orderRepository.findOrderById(orderId);
        if (order == null) {
            log.warn("【超时取消】订单不存在，ID: {}", orderId);
            return;
        }

        // 2. 校验状态：若已不是 PENDING，忽略取消
        if (order.getStatus() != OrderStatus.PENDING) {
            log.info("【超时取消】订单状态已变更，忽略取消，ID: {}, 当前状态: {}", orderId, order.getStatus());
            return;
        }

        // 2.1 关单前强制向三方支付平台发起一次对账查询，防止网络延迟或掉单导致误取消
        try {
            Result<String> reconcileResult = orderReconciliationAppService.reconcileOrder(orderId);
            if (reconcileResult.isSuccess()) {
                // 如果对账成功，说明用户在三方已支付，已通过 reconcileOrder 内部的补单逻辑更新为了支付状态
                log.warn("【超时取消】检测到该订单在三方已支付（发生掉单），已成功触发补单。取消关单逻辑，订单ID: {}", orderId);
                return;
            }
        } catch (Exception e) {
            log.error("【超时取消】前置对账异常，订单ID: {}", orderId, e);
        }

        // 3. 执行取消动作，更改状态为 CANCELLED
        order.cancel();
        orderRepository.save(order);

        // 3.1 移除 Redis 超时检测记录
        orderTimeoutProvider.cancelTimeout(orderId);



        // 5. 退回优惠券
        if (order.getCouponId() != null) {
            try {
                couponAppService.refund(order.getCouponId());
                log.info("【超时取消】订单优惠券已退回，ID: {}, 优惠券ID: {}", orderId, order.getCouponId());
            } catch (Exception e) {
                log.error("【超时取消】订单优惠券退回异常，ID: {}, 优惠券ID: {}", orderId, order.getCouponId(), e);
                throw e;
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        // 1. 查找订单
        Order order = orderRepository.findOrderById(orderId);
        if (order == null) {
            log.warn("【订单取消】订单不存在，ID: {}", orderId);
            return;
        }

        // 2. 校验状态：若已不是 PENDING，忽略取消
        if (order.getStatus() != OrderStatus.PENDING) {
            log.info("【订单取消】订单状态已变更，忽略取消，ID: {}, 当前状态: {}", orderId, order.getStatus());
            return;
        }

        // 3. 执行取消动作，更改状态为 CANCELLED
        order.cancel();
        orderRepository.save(order);

        // 3.1 移除 Redis 超时检测记录
        orderTimeoutProvider.cancelTimeout(orderId);

        // 4. 退回优惠券
        if (order.getCouponId() != null) {
            try {
                couponAppService.refund(order.getCouponId());
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
                .couponName(couponQueryVO == null ? "未使用优惠券" :couponQueryVO.getName())
                .couponId(couponQueryVO == null ? null : couponQueryVO.getId())
                .memberId(memberVO == null ? null :memberVO.getId())
                .memberName(memberVO == null ? "未查询到套餐信息" :memberVO.getName())
                .payableAmount(payableAmount)
                .payTime(order.getPayTime())
                .build();
    }
}

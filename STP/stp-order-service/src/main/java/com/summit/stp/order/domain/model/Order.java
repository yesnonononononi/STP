package com.summit.stp.order.domain.model;


import com.summit.stp.shared.domain.model.PayType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 订单 (领域聚合根)
 * 1. 彻底移除基础设施层框架注解 (MyBatis-Plus `@TableField`)
 * 2. 移除全局 `@Data` 与不安全的 Setter，改为只读属性 `@Getter` 与领域动作
 * 3. 使用 `OrderStatus` 值对象取代原始 `int`
 */
@Getter
@Builder
@EqualsAndHashCode
public class Order {
    private final Long id;          // 订单唯一ID (聚合根标识)
    private final Long creatorId;
    private final BigDecimal amount; // 支付金额
    private final PayType payType;   // 支付渠道类型
    private final String to;         // 收款方账户/地址
    private final String sign;       // 支付签名数据
    private OrderStatus status;      // 订单状态
    private final Long packageId;
    private final Integer quantity;
    private final Long couponId;
    private final BigDecimal unitPrice;
    private final BigDecimal discountAmount;
    private final Timestamp createTime;
    private Timestamp updateTime;
    private Timestamp payTime;








    /**
     * 确认并完成支付
     */
    public void payComplete() {
        if (this.status == OrderStatus.PAID || this.status == OrderStatus.COMPLETED) {
            return;
        }
        if (this.status != OrderStatus.PENDING && this.status != OrderStatus.CANCELLED) {
            throw new IllegalStateException("只有待支付或已取消的订单才能确认完成支付");
        }
        this.status = OrderStatus.PAID;
        this.payTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }

    /**
     * 取消订单
     */
    public void cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("只有待支付的订单才能执行取消动作");
        }
        this.status = OrderStatus.CANCELLED;
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }
}

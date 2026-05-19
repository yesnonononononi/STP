package com.summit.stp.payment.domain.model;

import com.summit.stp.payment.infrastructure.Enum.OrderStatus;
import com.summit.stp.payment.infrastructure.Enum.PayType;
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
public class Order {
    private final Long id;          // 订单唯一ID (聚合根标识)
    private final BigDecimal amount; // 支付金额
    private final PayType payType;   // 支付渠道类型
    private final String to;         // 收款方账户/地址
    private final String sign;       // 支付签名数据
    private OrderStatus status;      // 订单状态
    private final Timestamp createTime;
    private Timestamp updateTime;

    /**
     * 业务创建新订单的构造方法
     */
    public Order(Long id, BigDecimal amount, PayType payType, String to, String sign) {
        if (id == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("订单金额不能小于或等于0");
        }
        this.id = id;
        this.amount = amount;
        this.payType = payType;
        this.to = to;
        this.sign = sign;
        this.status = OrderStatus.PENDING; // 初始为待支付状态
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = this.createTime;
    }

    /**
     * 全参数私有构造器，专用于从基础设施层(Reconstitution)还原领域状态
     */
    private Order(Long id, BigDecimal amount, PayType payType, String to, String sign, OrderStatus status, Timestamp createTime, Timestamp updateTime) {
        this.id = id;
        this.amount = amount;
        this.payType = payType;
        this.to = to;
        this.sign = sign;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    /**
     * 基础设施层重建/还原订单对象的静态工厂方法
     */
    public static Order reconstitute(Long id, BigDecimal amount, PayType payType, String to, String sign, Integer status, Timestamp createTime, Timestamp updateTime) {
        return new Order(
                id, 
                amount, 
                payType, 
                to, 
                sign, 
                OrderStatus.fromCode(status), 
                createTime, 
                updateTime
        );
    }



    /**
     * 确认并完成支付
     */
    public void payComplete() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("只有待支付的订单才能确认完成支付");
        }
        this.status = OrderStatus.PAID;
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

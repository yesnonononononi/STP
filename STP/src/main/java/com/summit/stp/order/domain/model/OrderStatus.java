package com.summit.stp.order.domain.model;

import lombok.Getter;
import com.summit.stp.shared.exception.ParameterException;

/**
 * 订单状态值对象 (枚举)
 */


@Getter
public enum OrderStatus {
    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String description;

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OrderStatus fromCode(Integer code) {
        if (code == null) {
            return PENDING;
        }
        for (OrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new ParameterException("未知的订单状态码: " + code);
    }
}

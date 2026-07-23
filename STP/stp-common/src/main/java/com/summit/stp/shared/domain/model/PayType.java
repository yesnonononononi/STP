package com.summit.stp.shared.domain.model;

import com.summit.stp.shared.exception.ParameterException;
import lombok.Getter;

/**
 * 支付类型枚举 - 跨服务共享的领域枚举
 */
@Getter
public enum PayType {
    WX_PAY("wxpay", 1),
    ALI_PAY("alipay", 2);

    private final String type;
    private final int code;

    PayType(String type, int code) {
        this.type = type;
        this.code = code;
    }

    public static PayType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayType payType : values()) {
            if (payType.code == code) {
                return payType;
            }
        }
        throw new ParameterException("未知的支付类型编码: " + code);
    }

    public static PayType fromType(String type) {
        if (type == null || type.isEmpty()) {
            return null;
        }
        for (PayType payType : values()) {
            if (payType.type.equalsIgnoreCase(type)) {
                return payType;
            }
        }
        throw new ParameterException("未知的支付类型: " + type);
    }
}

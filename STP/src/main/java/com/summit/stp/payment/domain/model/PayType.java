package com.summit.stp.payment.domain.model;

import com.summit.stp.shared.exception.ParameterException;
import lombok.Getter;

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

    /**
     * 根据 type 字段查找枚举（用于回调）
     */
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
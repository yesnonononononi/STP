package com.summit.stp.payment.infrastructure.Enum;

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
        throw new IllegalArgumentException("未知的支付类型编码: " + code);
    }
}
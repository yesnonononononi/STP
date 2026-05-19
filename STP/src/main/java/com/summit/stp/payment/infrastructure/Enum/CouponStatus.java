package com.summit.stp.payment.infrastructure.Enum;

import lombok.Getter;

@Getter
public enum CouponStatus {
    USE("已使用",0),
    NOT_USE("未使用",1),
    EXPIRED("已过期",2);
    private final String description;
    private final int code;
    CouponStatus(String description, int code) {
        this.description = description;
        this.code = code;
    }
    public static CouponStatus fromCode(Integer code) {
        for (CouponStatus value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid CouponStatus code: " + code);
    }


}

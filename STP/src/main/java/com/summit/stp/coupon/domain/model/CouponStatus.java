package com.summit.stp.coupon.domain.model;

import lombok.Getter;
import com.summit.stp.shared.exception.ParameterException;

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
        throw new ParameterException("Invalid CouponStatus code: " + code);
    }


}

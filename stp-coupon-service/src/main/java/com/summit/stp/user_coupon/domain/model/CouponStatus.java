package com.summit.stp.user_coupon.domain.model;

import lombok.Getter;

@Getter
public enum CouponStatus {
    NOT_USE(0, "未使用"),
    USE(1, "已使用"),
    EXPIRED(2, "已过期");

    private final int code;
    private final String description;

    CouponStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static CouponStatus fromCode(int code) {
        for (CouponStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}

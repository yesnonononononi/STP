package com.summit.stp.payment.domain.model;

public enum PaySuccessType {
    VIP("vip支付",1),
    SUPER_VIP("超级vip支付",2);
    private String name;
    private int code;
    PaySuccessType(String name, int code) {
        this.name = name;
        this.code = code;
    }
    public PaySuccessType fromCode(Integer code) {
        for (PaySuccessType paySuccessType : PaySuccessType.values()) {
            if (paySuccessType.code == code) {
                return paySuccessType;
            }
        }
        return null;
    }

    public PaySuccessType fromName(String name) {
        for (PaySuccessType paySuccessType : PaySuccessType.values()) {
            if (paySuccessType.name.equals(name)) {
                return paySuccessType;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
    public int getCode() {
        return code;
    }
}

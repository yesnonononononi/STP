package com.summit.stp.userAuth.domain.model;

public enum ResetType {
    PHONE("通过手机号+验证码找回",1001),
    EMAIL("通过邮箱找回",1002);
    private final String name;
    private final int code;
    ResetType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static ResetType fromCode(Integer i) {
        if (i == null) {
            return null;
        }
        for (ResetType value : values()) {
            if (value.code == i) {
                return value;
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

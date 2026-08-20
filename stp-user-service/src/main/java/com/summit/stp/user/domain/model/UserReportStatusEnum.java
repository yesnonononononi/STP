package com.summit.stp.user.domain.model;

import lombok.Getter;

@Getter
public enum UserReportStatusEnum {
    PENDING(0, "待处理"),
    IGNORED(1, "已忽略"),
    PROCESSED(2, "已处置");

    private final int code;
    private final String description;

    UserReportStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static UserReportStatusEnum fromCode(Integer code) {
        if (code == null) return PENDING;
        for (UserReportStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return PENDING;
    }
}

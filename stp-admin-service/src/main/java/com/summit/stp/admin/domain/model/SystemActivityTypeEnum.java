package com.summit.stp.admin.domain.model;

import lombok.Getter;

@Getter
public enum SystemActivityTypeEnum {
    WARN("WARN", "风险预警"),
    INFO("INFO", "系统日志"),
    ORDER("ORDER", "大额交易"),
    AUDIT("AUDIT", "待审事件");

    private final String code;
    private final String description;

    SystemActivityTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SystemActivityTypeEnum fromCode(String code) {
        if (code == null) return INFO;
        for (SystemActivityTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return INFO;
    }
}

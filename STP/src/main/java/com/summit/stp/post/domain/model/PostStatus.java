package com.summit.stp.post.domain.model;

import lombok.Getter;

@Getter
public enum PostStatus {
    NORMAL(1, "正常"),
    DELETED(2, "删除"),
    BLOCKED(3, "封禁"),
    REPORTED(4, "举报"),
    UNKNOWN(5, "未知"),
    DRAFT(6, "草稿");
    private final int code;
    private final String desc;
    PostStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PostStatus fromCode(int code) {
        for (PostStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return UNKNOWN;
    }
}

package com.summit.stp.post.domain.model;

import lombok.Getter;

@Getter
public enum PostType {
    TEXT(1, "文本"),
    IMAGE(2, "图片"),
    VIDEO(3, "视频"),
    AUDIO(4, "音频"),
    LINK(5, "链接"),
    FILE(6, "文件"),
    UNKNOWN(7, "未知");
    private final int code;
    private final String desc;
    PostType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public static PostType fromCode(int code) {
        for (PostType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
    public static PostType fromName(String name) {
        if (name == null) {
            return UNKNOWN;
        }
        for (PostType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}

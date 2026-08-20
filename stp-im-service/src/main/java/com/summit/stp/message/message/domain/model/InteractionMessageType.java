package com.summit.stp.message.message.domain.model;

import lombok.Getter;

@Getter
public enum InteractionMessageType {
    LIKE(1, "点赞"),
    COMMENT(2, "评论"),
    FOLLOW(3, "关注"),
    MENTION(4, "提及"),
    REPLY(5, "回复"),
    COLLECT(6, "收藏");

    private final Integer code;
    private final String description;

    InteractionMessageType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static InteractionMessageType fromCode(Integer code) {
        for (InteractionMessageType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}

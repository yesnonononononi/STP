package com.summit.stp.comment.comment.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CommentType {
    IMAGE("图片",1),
    VIDEO("视频",2),
    TEXT("文字",3),
    AUDIO("音频",4);
    private final String name;
    private final Integer code;
    
    CommentType(String name, Integer code) {
        this.name = name;
        this.code = code;
    }
    
    @JsonValue
    public Integer getCode() {
        return code;
    }
    
    @JsonCreator
    public static CommentType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CommentType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid comment type code: " + code);
    }
}

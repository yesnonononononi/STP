package com.summit.stp.message.entertainment.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Builder
@Getter
public class EmojiPackage {
    private final Long id;
    private String name;
    private String description;
    private String coverImage;
    private Integer status;
    private Timestamp createTime;
    private Timestamp updateTime;
}

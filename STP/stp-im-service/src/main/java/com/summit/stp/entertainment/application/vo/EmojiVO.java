package com.summit.stp.entertainment.application.vo;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class EmojiVO {
    private Long id;
    private String tiny;
    private String name;
    private Integer type;
    private  String url;
}

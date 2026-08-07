package com.summit.stp.entertainment.application.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmojiVO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String tiny;
    private String name;
    private Integer type;
    private  String url;
}

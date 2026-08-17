package com.summit.stp.post.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 帖子简要信息 - 跨服务 Feign 调用返回的 VO 契约
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSimpleVO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long creatorId;
    private String title;
    private String content;
    private Integer status;
}


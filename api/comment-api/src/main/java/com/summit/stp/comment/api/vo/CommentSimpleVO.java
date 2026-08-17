package com.summit.stp.comment.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论简要信息 - 跨服务 Feign 调用返回的 VO 契约
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentSimpleVO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long postId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long publisherId;
    private String content;
    private Integer type;
    private Boolean isLiked;
}


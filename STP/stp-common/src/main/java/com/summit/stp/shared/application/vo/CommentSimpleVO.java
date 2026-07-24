package com.summit.stp.shared.application.vo;

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
    private Long id;
    private Long postId;
    private Long parentId;
    private Long publisherId;
    private String content;
    private Integer type;
}

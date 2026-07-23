package com.summit.stp.shared.application.vo;

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
    private Long id;
    private Long creatorId;
    private String title;
    private String content;
    private Integer status;
}

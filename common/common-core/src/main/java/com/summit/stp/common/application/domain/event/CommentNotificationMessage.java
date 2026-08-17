package com.summit.stp.common.application.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 评论通知消息 - 跨服务共享的 MQ 事件契约
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentNotificationMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long commentId;
    private Long postId;
    private Long parentId;
    private String commentContent;
    private Long triggerUserId;
    private String postTitle;
    private Long postCreatorId;
    private String clientIp;
    private Long timestamp;
}

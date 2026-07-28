package com.summit.stp.common.application.domain.event;

import lombok.Data;

/**
 * 帖子发布事件 - 跨服务共享的 MQ 事件契约
 */
@Data
public class PostPublishEvent {
    private Long postId;
    private Long userId;
}

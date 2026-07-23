package com.summit.stp.shared.domain.event;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

/**
 * 帖子互动事件（点赞/收藏）- 跨服务共享的事件契约
 */
@Getter
@Builder
public class PostInteractionEvent {
    private final Long postId;
    private final Long userId;
    private final String interactionType; // "LIKE" 或 "COLLECT"
    private final Instant timestamp;
}

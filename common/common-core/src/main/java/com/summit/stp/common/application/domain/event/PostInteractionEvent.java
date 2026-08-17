package com.summit.stp.common.application.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;

/**
 * 帖子互动事件（点赞/收藏）- 跨服务共享的事件契约
 */
@Getter
@Builder
@AllArgsConstructor
public class PostInteractionEvent implements Serializable {
    private final Boolean isOnce;
    private final Boolean offset;
    private final Long postId;
    private final Long userId;
    private final String interactionType; // "LIKE" 或 "COLLECT"
    private final Instant timestamp;


    @Override
    public String toString() {
        return String.format("PostInteractionEvent(isOnce=%s,postId=%s,userId=%s,interactionType=%s,timestamp=%s",isOnce,postId,userId,interactionType,timestamp);
    }
}

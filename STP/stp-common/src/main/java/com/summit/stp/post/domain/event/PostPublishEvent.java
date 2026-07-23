package com.summit.stp.post.domain.event;

import lombok.Data;

@Data
public class PostPublishEvent {
    private Long postId;
    private Long userId;
}

package com.summit.stp.message.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class InteractionMessage {
    private final Long id;
    private final Long publicId;
    private final Long senderId;
    private String senderAvatar;
    private String senderName;
    private final Long receiverId;
    private final Integer messageType;
    private String content;
    private final Long associateContent;
    private final Long postId;
    private String associateTitle;
    private Integer isDel;
    private Instant createTime;
    private Instant updateTime;
}

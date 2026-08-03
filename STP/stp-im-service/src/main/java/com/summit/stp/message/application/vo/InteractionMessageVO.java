package com.summit.stp.message.application.vo;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class InteractionMessageVO {
    private Long publicId;
    private Long senderId;
    private String senderAvatar;
    private String senderName;
    private Long receiverId;
    private Integer messageType;
    private String content;
    private Long associateContent;
    private Long postId;
    private Instant createTime;
    private Boolean isLike;
    // 扩展字段，用于前端右侧区域的帖子标题或回复的评论快照展示
    private String associateContentTitle;
}

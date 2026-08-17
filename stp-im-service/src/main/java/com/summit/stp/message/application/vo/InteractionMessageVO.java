package com.summit.stp.message.application.vo;

import com.summit.stp.common.annotation.PublicId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class InteractionMessageVO {
    @PublicId
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long senderId;
    private String senderAvatar;
    private String senderName;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long receiverId;
    private Integer messageType;
    private String content;
    private Long associateContent;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long postId;
    private Instant createTime;
    private Boolean isLike;
    // 扩展字段，用于前端右侧区域的帖子标题或回复的评论快照展示
    private String associateContentTitle;
}

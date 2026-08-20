package com.summit.stp.message.message.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@TableName("interaction_message")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionMessagePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private String senderAvatar;
    private String senderName;
    private Long receiverId;
    private Integer messageType;
    private String content;
    private Long associateContent;
    private Long postId;
    private String associateTitle;
    private Integer isDel;
    private Instant createTime;
    private Instant updateTime;
}

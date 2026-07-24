package com.summit.stp.message.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
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
    @TableField(fill = FieldFill.INSERT)
    private Long publicId;
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

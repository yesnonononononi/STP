package com.summit.stp.message.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@TableName("session")
public class SessionPO {
    @ApiModelProperty(notes = "会话id")
    @TableId(type = IdType.INPUT)
    private Long id;
    @ApiModelProperty(notes = "会话类型")
    private Integer type;
    @ApiModelProperty(notes = "最后一条消息id")
    private Long lastMessageId;
    @ApiModelProperty(notes = "最后一条消息内容")
    private String lastMessageContent;
    @ApiModelProperty(notes = "最后发送者id")
    private Long lastSenderId;
    @ApiModelProperty(notes = "最后发送时间")
    private Instant lastTime;
    @ApiModelProperty(notes = "创建时间")
    private Instant createTime;
    @ApiModelProperty(notes = "更新时间")
    private Instant updateTime;
}

package com.summit.stp.message.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
@Builder
@Getter
@TableName("private_message")
public class PrivateMessagePO {
    @ApiModelProperty(notes = "消息id")
    @TableId(type = IdType.AUTO)
    private Long id;
    @ApiModelProperty(notes = "发送者id")
    private Long userId;
    @ApiModelProperty(notes = "接收者id")
    private Long receiverId;
    @ApiModelProperty(notes = "发送时间")
    private Instant sendTime;
    @ApiModelProperty(notes = "消息内容")
    private String content;
    @ApiModelProperty(notes = "图片")
    private String image;
    @ApiModelProperty(notes = "音频")
    private String audio;
    @ApiModelProperty(notes = "视频")
    private String video;
    @ApiModelProperty(notes = "消息类型")
    private Integer type;
    @ApiModelProperty(notes = "消息状态")
    private Integer status;
    @ApiModelProperty(notes = "会话id")
    private Long sessionId;

}

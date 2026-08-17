package com.summit.stp.im.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息展示信息 - 跨服务共享的 VO 契约
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "MessageVO", description = "消息展示信息")
public class MessageVO {
    @ApiModelProperty("消息ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("发送者id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    @ApiModelProperty("接收者id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long receiverId;

    @ApiModelProperty("发送时间")
    private String sendTime;

    @ApiModelProperty("消息内容")
    private String content;

    @ApiModelProperty("图片地址")
    private String image;

    @ApiModelProperty("音频地址")
    private String audio;

    @ApiModelProperty("视频地址")
    private String video;

    @ApiModelProperty("消息类型 (1-图片, 2-文本, 3-音频, 4-视频)")
    private Integer type;

    @ApiModelProperty("消息状态 (1-未读, 2-已读, 3-撤回)")
    private Integer status;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long sessionId;

    @ApiModelProperty("扩展字段")
    private String extra;
}


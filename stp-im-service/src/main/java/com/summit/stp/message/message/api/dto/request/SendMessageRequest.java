package com.summit.stp.message.message.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "SendMessageRequest", description = "发送消息请求体")
public class SendMessageRequest {
    @ApiModelProperty("消息ID")
    private Long msgId;

    @ApiModelProperty("接收者ID")
    private Long receiverId;

    @ApiModelProperty("消息内容")
    private String content;

    @ApiModelProperty("图片地址")
    private String image;

    @ApiModelProperty("音频地址")
    private String audio;

    @ApiModelProperty("视频地址")
    private String video;

    @ApiModelProperty("发送时间")
    private String sendTime;

    @ApiModelProperty("消息类型 (1-图片, 2-文本, 3-音频, 4-视频)")
    private Integer type;
}


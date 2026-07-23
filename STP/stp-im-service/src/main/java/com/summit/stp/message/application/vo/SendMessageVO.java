package com.summit.stp.message.application.vo;

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
@ApiModel(value = "SendMessageVO", description = "发送消息返回结果")
public class SendMessageVO {
    @ApiModelProperty("消息ID")
    private Long messageId;
}


package com.summit.stp.message.application.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.summit.stp.common.application.vo.MessageVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "MessageListVO", description = "消息列表展示信息")
public class MessageListVO {
    @ApiModelProperty("消息列表")
    private List<MessageVO> messages;

    @ApiModelProperty("是否还有更多")
    private boolean hasMore;

    @ApiModelProperty("游标ID")
    private Long cursorId;

    @ApiModelProperty("会话ID")
    private Long sessionId;
}


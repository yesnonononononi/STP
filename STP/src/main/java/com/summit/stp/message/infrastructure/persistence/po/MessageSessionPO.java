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
@TableName("message_session")
public class MessageSessionPO {
    @ApiModelProperty(notes = "会话id")
    @TableId(type = IdType.INPUT)
    private Long id;
    @ApiModelProperty(notes = "当前用户id")
    private Long userId;
    @ApiModelProperty(notes = "目标用户id")
    private Long targetId;
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
    @ApiModelProperty(notes = "未读消息数量")
    private Integer unreadCountForUser;
    @ApiModelProperty(notes = "未读消息数量")
    private Integer unreadCountForTarget;
    @ApiModelProperty(notes = "是否置顶")
    private Integer isTop;
    @ApiModelProperty(notes = "是否免打扰")
    private Integer isMute;
    @ApiModelProperty(notes = "草稿")
    private String draft;
    @ApiModelProperty(notes = "是否隐藏")
    private Integer isHidden;
    @ApiModelProperty(notes = "目标用户昵称")
    private String targetNickName;
    @ApiModelProperty(notes = "目标用户头像")
    private String targetAvatar;
    @ApiModelProperty(notes = "创建时间")
    private Instant createTime;
    @ApiModelProperty(notes = "更新时间")
    private Instant updateTime;
}

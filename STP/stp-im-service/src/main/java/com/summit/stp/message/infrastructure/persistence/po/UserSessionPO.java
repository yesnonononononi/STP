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
@TableName("user_session")
public class UserSessionPO {
    @ApiModelProperty(notes = "自增主键")
    @TableId(type = IdType.AUTO)
    private Long id;
    @ApiModelProperty(notes = "当前用户id")
    private Long userId;
    @ApiModelProperty(notes = "当前会话对象id")
    private Long targetId;
    @ApiModelProperty(notes = "会话id")
    private Long sessionId;
    @ApiModelProperty(notes = "是否置顶")
    private Integer isTop;
    @ApiModelProperty(notes = "是否免打扰")
    private Integer isMute;
    @ApiModelProperty(notes = "草稿")
    private String draft;
    @ApiModelProperty(notes = "未读消息数量")
    private Integer unreadCount;
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

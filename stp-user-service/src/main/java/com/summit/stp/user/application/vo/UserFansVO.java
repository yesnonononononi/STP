package com.summit.stp.user.application.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UserFansVO", description = "用户粉丝展示数据")
public class UserFansVO {
    @ApiModelProperty("关注关系记录ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ApiModelProperty("粉丝用户ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @ApiModelProperty("粉丝昵称")
    private String nick;

    @ApiModelProperty("粉丝头像")
    private String avatar;

    @ApiModelProperty("我是否已关注该粉丝：true-已关注/已回关，false-未关注")
    private Boolean followed;
}

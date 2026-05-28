package com.summit.stp.user.application.vo;

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
@ApiModel(value = "UserSimpleVO", description = "用户公开展示简单信息")
public class UserSimpleVO {
    @ApiModelProperty("用户ID")
    private Long id;
    @ApiModelProperty("用户昵称")
    private String nick;
    @ApiModelProperty("用户头像")
    private String avatar;
    @ApiModelProperty("会员等级")
    private String memberLevel;
    @ApiModelProperty("等级名称")
    private String memberLevelName;
    @ApiModelProperty("用户简介")
    private String introduction;
    @ApiModelProperty("用户粉丝")
    private Long fans;
    @ApiModelProperty("被赞")
    private Long liked;
    @ApiModelProperty("用户帖子")
    private Long topic;
    @ApiModelProperty("用户性别")
    private String gender;
    @ApiModelProperty("用户IP")
    private String ip;
    @ApiModelProperty("会员类型")
    private String vipType;
    @ApiModelProperty("会员等级图标")
    private String vipConfigIcon;
    @ApiModelProperty("是否关注")
    private boolean followed;
}

package com.summit.stp.user.application.vo;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 用户简单信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {
    @ApiModelProperty("用户ID")
    private Long id;
    @ApiModelProperty("用户昵称")
    private String nick;
    @ApiModelProperty("用户头像")
    private String avatar;
    @ApiModelProperty("电话号码")
    private String phone;
    @ApiModelProperty("用户邮箱")
    private String email;
    @ApiModelProperty("会员等级")
    private String memberLevel;
    @ApiModelProperty("用户IP")
    private String ip;
    @ApiModelProperty("用户点赞")
    private String liked;
    @ApiModelProperty("用户话题")
    private String topic;
    @ApiModelProperty("用户粉丝")
    private String fans;
    @ApiModelProperty("会员类型")
    private String vipType;
    @ApiModelProperty("用户简介")
    private String introduction;
    @ApiModelProperty("会员到期时间")
    private String vipExpireDate;
    @ApiModelProperty("会员等级图标")
    private String vipConfigIcon;
    @ApiModelProperty("性别")
    private Integer gender;
    @ApiModelProperty("年龄")
    private Integer age;
    @ApiModelProperty("是否关注")
    private boolean followed;
    @ApiModelProperty("背景图")
    private String bgImage;

}

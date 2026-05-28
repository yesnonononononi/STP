package com.summit.stp.user.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UserProfileUpdateRequest", description = "修改用户基本资料请求参数")
public class UserProfileUpdateRequest {
    @ApiModelProperty(value = "用户昵称", example = "Summit")
    private String nick;

    @ApiModelProperty(value = "头像URL")
    private String avatar;

    @ApiModelProperty(value = "电子邮箱", example = "summit@example.com")
    private String email;

    @ApiModelProperty(value = "邮箱验证码（换绑/修改邮箱时必填）", example = "1234")
    private String verifyCode;

    @ApiModelProperty(value = "个人简介")
    private String introduction;

    @ApiModelProperty(value = "性别", example = "1 男 0 女")
    private Integer gender;

    @ApiModelProperty(value = "年龄", example = "25")
    private Integer age;
}

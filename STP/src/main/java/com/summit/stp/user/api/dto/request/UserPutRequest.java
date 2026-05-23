package com.summit.stp.user.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UserPutRequest", description = "修改用户信息请求参数")
public class UserPutRequest {
    @ApiModelProperty(value = "用户名", example = "Summit")
    private String uname;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "旧密码（修改密码时使用）", example = "123456")
    private String oldPassword;

    @ApiModelProperty(value = "新密码（修改密码时使用）", example = "654321")
    private String newPassword;

    @ApiModelProperty(value = "手机号码", example = "13800138000")
    private String phoneNumber;

    @ApiModelProperty(value = "状态码")
    private Integer statusCode;

    @ApiModelProperty(value = "验证码", example = "1234")
    private Integer verifyCode;
}

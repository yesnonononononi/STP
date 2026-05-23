package com.summit.stp.userAuth.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "RegisterRequest", description = "用户注册请求参数")
public class RegisterRequest {
    @ApiModelProperty(value = "手机号码", required = true, example = "13800138000")
    private String phoneNumber;

    @ApiModelProperty(value = "密码", required = true, example = "123456")
    private String password;

    @ApiModelProperty(value = "短信验证码", required = true, example = "1234")
    private String verifyCode;
}

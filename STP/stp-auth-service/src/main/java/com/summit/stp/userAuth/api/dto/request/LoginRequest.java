package com.summit.stp.userAuth.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "LoginRequest", description = "用户登录请求参数")
public class LoginRequest {
    @ApiModelProperty(value = "用户名/手机号", required = true, example = "13800138000")
    private String username;

    @ApiModelProperty(value = "密码", required = true, example = "123456")
    private String password;
}

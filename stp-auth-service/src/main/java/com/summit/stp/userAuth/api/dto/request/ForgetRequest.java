package com.summit.stp.userAuth.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.Timestamp;
@Data
@ApiModel(value = "ForgetRequest", description = "忘记密码/重置密码请求参数")
public class ForgetRequest {
    @ApiModelProperty(value = "手机号码", required = true, example = "13800138000")
    private String phoneNumber;

    @ApiModelProperty(value = "短信验证码", required = true, example = "1234")
    private String verifyCode;

    @ApiModelProperty(value = "新密码", required = true, example = "123456")
    private String password;

    @ApiModelProperty(value = "重置方式 (1: 手机, 2: 邮箱)", required = true, example = "1")
    private Integer resetType;

    @ApiModelProperty(value = "提交时间")
    private Timestamp commitTime;
}

package com.summit.stp.user.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UserPhoneBindRequest", description = "修改/绑定手机号码请求参数")
public class UserPhoneBindRequest {
    @ApiModelProperty(value = "新手机号码", required = true, example = "13800138000")
    private String phoneNumber;

    @ApiModelProperty(value = "验证码", required = true, example = "1234")
    private Integer verifyCode;
}

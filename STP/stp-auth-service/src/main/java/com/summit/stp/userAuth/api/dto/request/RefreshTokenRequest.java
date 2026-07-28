package com.summit.stp.userAuth.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "RefreshTokenRequest", description = "刷新Token请求参数")
public class RefreshTokenRequest {
    @ApiModelProperty(value = "刷新Token凭证", required = true)
    private String refreshToken;

    @ApiModelProperty(value = "用户名/手机号", required = true, example = "13800138000")
    private String username;
}

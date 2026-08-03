package com.summit.stp.userAuth.application.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新 Token 结果 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "RefreshTokenVO", description = "刷新Token返回结果")
public class RefreshTokenVO {
    @ApiModelProperty(value = "新访问Token")
    private String token;

    @ApiModelProperty(value = "新刷新Token")
    private String refreshToken;
}

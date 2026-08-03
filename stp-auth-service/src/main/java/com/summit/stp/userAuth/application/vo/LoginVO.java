package com.summit.stp.userAuth.application.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录结果 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "LoginVO", description = "登录成功返回结果")
public class LoginVO {
    @ApiModelProperty(value = "访问Token")
    private String token;

    @ApiModelProperty(value = "刷新Token（用于Token过期时刷新访问Token）")
    private String refreshToken;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "过期时间戳(毫秒)")
    private Long expireTime;
}

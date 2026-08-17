package com.summit.stp.user.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户认证信息VO - 专用于认证服务内部调用
 * 包含密码字段，不对外暴露
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthVO {
    
    @ApiModelProperty("用户ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("加密密码")
    private String password;
    
    @ApiModelProperty("手机号")
    private String phoneNumber;
    
    @ApiModelProperty("用户状态：1激活，0封禁")
    private Integer statusCode;
}

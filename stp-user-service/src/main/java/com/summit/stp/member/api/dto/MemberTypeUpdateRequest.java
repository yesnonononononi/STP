package com.summit.stp.member.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改/更新会员类型请求参数 - 跨服务共享的 DTO 契约
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "MemberTypeUpdateRequest", description = "修改/更新会员类型请求参数")
public class MemberTypeUpdateRequest {
    @ApiModelProperty(value = "类型ID", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "类型名称", example = "月度会员")
    private String name;

    @ApiModelProperty(value = "类型描述", example = "购买后享受30天超级会员权益")
    private String description;

    @ApiModelProperty(value = "状态 (0: 禁用, 1: 启用)", example = "1")
    private Integer status;
}

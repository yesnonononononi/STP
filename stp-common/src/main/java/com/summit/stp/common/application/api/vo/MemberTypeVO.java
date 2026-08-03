package com.summit.stp.common.application.api.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会员类型数据承载对象 - 跨服务共享的 VO 契约
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "MemberTypeVO", description = "会员类型数据承载对象")
public class MemberTypeVO {
    @ApiModelProperty(value = "类型ID")
    private Long id;

    @ApiModelProperty(value = "类型名称")
    private String name;

    @ApiModelProperty(value = "类型描述")
    private String description;
}

package com.summit.stp.shared.application.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员套餐数据承载对象 - 跨服务共享的 VO 契约
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "MemberVO", description = "会员套餐数据承载对象")
public class MemberVO implements Serializable {
    @ApiModelProperty(value = "套餐ID")
    private Long id;

    @ApiModelProperty(value = "套餐名称")
    private String name;

    @ApiModelProperty(value = "套餐单价")
    private BigDecimal price;

    @ApiModelProperty(value = "折扣")
    private Double discount;

    @ApiModelProperty(value = "数量")
    private Integer quantity;

    @ApiModelProperty(value = "套餐描述")
    private String description;

    @ApiModelProperty(value = "套餐有效期（天）")
    private Integer duration;

    @ApiModelProperty(value = "会员分类ID")
    private Long typeId;

    @ApiModelProperty(value = "会员分类名称")
    private String typeName;

    @ApiModelProperty(value = "会员优先级，数值越大优先级越高")
    private Integer priority;
}

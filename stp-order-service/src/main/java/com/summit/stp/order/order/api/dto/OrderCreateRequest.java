package com.summit.stp.order.order.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value = "OrderCreateRequest", description = "创建订单请求参数")
public class OrderCreateRequest {
    @ApiModelProperty(value = "优惠券ID", example = "1002")
    private Long couponId;

    @ApiModelProperty(value = "购买的会员套餐ID", required = true, example = "2001")
    private Long packageId;

    @ApiModelProperty(value = "购买数量", required = true, example = "1")
    private Integer quantity;

    @ApiModelProperty(value = "支付通道类型编码", required = true, example = "1")
    private Integer payType;
}

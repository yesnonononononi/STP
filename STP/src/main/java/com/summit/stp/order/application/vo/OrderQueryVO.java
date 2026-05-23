package com.summit.stp.order.application.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 订单查询响应 VO (位于 Application 层，表示查询返回契约)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "OrderQueryVO", description = "订单查询响应结果")
public class OrderQueryVO {
    @ApiModelProperty(value = "订单唯一ID")
    private Long orderId;

    @ApiModelProperty(value = "订单实付总金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "订单状态")
    private String status;

    @ApiModelProperty(value = "支付通道名称")
    private String payTypeName;

    @ApiModelProperty(value = "订单创建时间")
    private Timestamp createTime;

    @ApiModelProperty(value = "订单支付去向说明")
    private String toName;

    @ApiModelProperty(value = "应付金额")
    private BigDecimal payableAmount;

    @ApiModelProperty(value = "会员套餐名称")
    private String memberName;

    @ApiModelProperty(value = "会员套餐ID")
    private Long memberId;

    @ApiModelProperty(value = "优惠券名称")
    private String couponName;

    @ApiModelProperty(value = "优惠券ID")
    private Long couponId;

    @ApiModelProperty(value = "支付完成时间")
    private Timestamp payTime;


}

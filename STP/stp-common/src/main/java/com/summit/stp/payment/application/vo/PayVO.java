package com.summit.stp.payment.application.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 支付用例响应 VO (定义于应用层，表示完整的支付返回契约)
 */
@Data
@Builder
@ApiModel(value = "PayVO", description = "支付表单承载结果")
public class PayVO {
    @ApiModelProperty(value = "商户ID")
    private int pid;

    @ApiModelProperty(value = "系统内部订单唯一ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long orderId;

    @ApiModelProperty(value = "三方支付签名串")
    private String sign;

    @ApiModelProperty(value = "支付金额（字符串，最多2位小数）")
    private String money;

    @ApiModelProperty(value = "购买商品/会员名称")
    private String memberName;

    @ApiModelProperty(value = "支付方式 (alipay/wxpay等)")
    private String type;

    @ApiModelProperty(value = "支付去向（目标网关或动作）")
    private String to;

    @ApiModelProperty(value = "10位时间戳（秒）")
    private String timestamp;

    @ApiModelProperty(value = "同步返回地址")
    private String returnUrl;

    @ApiModelProperty(value = "异步通知回调地址")
    private String notifyUrl;

    @ApiModelProperty(value = "签名算法类型")
    private String signType;
}

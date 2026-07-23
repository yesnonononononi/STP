package com.summit.stp.shared.application.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付用例响应 VO - 跨服务共享的契约
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String timestamp;

    @ApiModelProperty(value = "同步返回地址")
    private String returnUrl;

    @ApiModelProperty(value = "异步通知回调地址")
    private String notifyUrl;

    @ApiModelProperty(value = "签名算法类型")
    private String signType;
}

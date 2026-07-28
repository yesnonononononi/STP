package com.summit.stp.payment.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "PayCallbackRequest", description = "支付回调通知请求参数")
public class PayCallbackRequest {
    @ApiModelProperty(value = "商户ID", example = "10001")
    private String pid;

    @ApiModelProperty(value = "支付平台订单号", example = "2021010122001412")
    private Long trade_no;

    @ApiModelProperty(value = "商户系统内部订单号", example = "13423452345")
    private Long out_trade_no;

    @ApiModelProperty(value = "API支付平台生成的订单号")
    private Long api_trade_no;

    @ApiModelProperty(value = "支付方式类型", example = "alipay")
    private String type;

    @ApiModelProperty(value = "交易完成状态", example = "TRADE_SUCCESS")
    private String trade_status;

    @ApiModelProperty(value = "订单添加/创建时间")
    private String addtime;

    @ApiModelProperty(value = "支付结束/完成时间")
    private String endtime;

    @ApiModelProperty(value = "商品名称", example = "月度超级会员")
    private String name;

    @ApiModelProperty(value = "支付交易金额", example = "19.90")
    private String money;

    @ApiModelProperty(value = "买家支付账号标识")
    private String buyer;

    @ApiModelProperty(value = "安全校验时间戳")
    private String timestamp;

    @ApiModelProperty(value = "支付回调数字签名签名串")
    private String sign;

    @ApiModelProperty(value = "签名类型", example = "MD5")
    private String sign_type;
}

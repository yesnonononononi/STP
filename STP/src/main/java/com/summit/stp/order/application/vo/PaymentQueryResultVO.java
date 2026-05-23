package com.summit.stp.order.application.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentQueryResultVO {

    /**
     * 返回状态码 (0为成功，其它值为失败)
     */
    private Integer code;

    /**
     * 返回提示或错误消息
     */
    private String msg;

    /**
     * 平台订单号
     */
    @JsonProperty("trade_no")
    private String tradeNo;

    /**
     * 商户订单号
     */
    @JsonProperty("out_trade_no")
    private String outTradeNo;

    /**
     * 接口订单号 (三方通道底层的单号)
     */
    @JsonProperty("api_trade_no")
    private String apiTradeNo;

    /**
     * 支付渠道方式 (如 alipay, wxpay)
     */
    private String type;

    /**
     * 支付状态: 0-未支付，1-已支付，2-已退款，3-已冻结，4-预授权
     */
    private Integer status;

    /**
     * 平台ID
     */
    private Integer pid;

    /**
     * 订单创建时间 (格式化字符串)
     */
    private String addtime;

    /**
     * 订单完成时间 (格式化字符串)
     */
    private String endtime;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品金额 (元)
     */
    private String money;

    /**
     * 已退款金额 (元)
     */
    private String refundmoney;

    /**
     * 业务扩展参数
     */
    private String param;

    /**
     * 支付用户标识
     */
    private String buyer;

    /**
     * 支付用户IP
     */
    private String clientip;

    /**
     * 10位时间戳 (秒)
     */
    private String timestamp;

    /**
     * 签名字符串
     */
    private String sign;

    /**
     * 签名类型
     */
    @JsonProperty("sign_type")
    private String signType;
}

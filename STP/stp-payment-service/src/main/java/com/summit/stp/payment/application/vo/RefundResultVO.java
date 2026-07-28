package com.summit.stp.payment.application.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResultVO {

    /**
     * 返回状态码 (0为成功，其它值为失败)
     */
    private Integer code;

    /**
     * 返回提示消息
     */
    private String msg;

    /**
     * 平台退款单号
     */
    @JsonProperty("refund_no")
    private String refundNo;

    /**
     * 商户退款单号
     */
    @JsonProperty("out_refund_no")
    private String outRefundNo;

    /**
     * 平台订单号
     */
    @JsonProperty("trade_no")
    private String tradeNo;

    /**
     * 退款金额
     */
    private String money;

    /**
     * 扣减账户余额金额
     */
    private String reducemoney;

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

package com.summit.stp.payment.application.command;

import lombok.Data;

@Data
public class PayCallbackCheckCommand {
    /**
     * 商户ID
     */
    private String pid;
    /**
     * 订单号
     */
    private Long trade_no;
    /**
     * 商户订单号
     */
    private Long out_trade_no;
    /**
     * API订单号
     */
    private Long api_trade_no;
    /**
     * 支付方式
     */
    private String type;
    /**
     * 交易状态
     */
    private String trade_status;
    /**
     * 交易时间
     */
    private String addtime;
    /**
     * 结束时间
     */
    private String endtime;
    /**
     * 商品名称
     */
    private String name;
    /**
     * 金额
     */
    private String money;
    /**
     * 买家
     */
    private String buyer;
    /**
     * 时间戳
     */
    private String timestamp;
    /**
     * 签名
     */
    private String sign;
    /**
     * 签名类型
     */
    private String sign_type;
}

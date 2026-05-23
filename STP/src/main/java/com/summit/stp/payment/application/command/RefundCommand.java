package com.summit.stp.payment.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundCommand {
    /**
     * 平台订单号 (与 outTradeNo 必须二选一)
     */
    private String tradeNo;

    /**
     * 商户订单号 (与 tradeNo 必须二选一)
     */
    private String outTradeNo;

    /**
     * 退款金额 (单位：元，例如: 1.00)
     */
    private BigDecimal money;

    /**
     * 商户退款单号 (可选，用于幂等避免重复请求)
     */
    private String outRefundNo;
}

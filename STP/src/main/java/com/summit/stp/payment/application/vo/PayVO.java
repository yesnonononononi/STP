package com.summit.stp.payment.application.vo;

import com.summit.stp.payment.infrastructure.Enum.PayType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 支付用例响应 VO (定义于应用层，表示完整的支付返回契约)
 */
@Data
@Builder
public class PayVO {
    private Long orderId;
    private String sign;
    private BigDecimal amount;
    private Long commodityName;
    private PayType payType;
    private String to;
    private Timestamp timestamp;
}

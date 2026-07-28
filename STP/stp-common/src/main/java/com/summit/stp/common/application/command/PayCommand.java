package com.summit.stp.common.application.command;

import com.summit.stp.common.application.domain.model.PayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 支付命令 - 跨服务共享的契约
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayCommand {
    private String username;
    private String memberName;
    private Timestamp timestamp;
    private BigDecimal amount;
    private Long orderId;
    private PayType payType;
}

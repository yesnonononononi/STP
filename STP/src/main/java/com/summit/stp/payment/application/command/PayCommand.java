package com.summit.stp.payment.application.command;

import com.summit.stp.payment.domain.model.PayType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;


@Builder
@Data
public class PayCommand {
    /**
     * 用户名
     */
    private String username;



    private String memberName;


    private Timestamp timestamp;

    private BigDecimal amount;
    /**
     * 订单id
     */
    private Long orderId;

    /**
     * 支付方式
     */
    private PayType payType;







}

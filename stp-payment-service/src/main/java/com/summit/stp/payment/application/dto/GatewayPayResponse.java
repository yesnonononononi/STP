package com.summit.stp.payment.application.dto;

import lombok.Data;

@Data
public class GatewayPayResponse {
    private Integer code;
    private String msg;
    private String trade_no;
    private String pay_type;
    private String pay_info;
    private String timestamp;
    private String sign;
    private String sign_type;
}

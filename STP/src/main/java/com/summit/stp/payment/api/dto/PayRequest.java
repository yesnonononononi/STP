package com.summit.stp.payment.api.dto;

import lombok.Data;

@Data
public class PayRequest {
    private String uname;
    private Long commodityId;
    //商品数量
    private Integer quantity;
    private String payType;
}

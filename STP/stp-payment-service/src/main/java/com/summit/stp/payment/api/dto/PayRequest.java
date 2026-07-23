package com.summit.stp.payment.api.dto;

import lombok.Data;

@Data
public class PayRequest {
    private String uname;
    private Long packageId;
    //商品数量
    private Integer quantity;
    private Long couponId;
    private Integer payType;

}

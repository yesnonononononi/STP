package com.summit.stp.payment.infrastructure.persistence.po;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ShoppingCartPO {
    private Long id;
    private Long uname;
    private Long commodityId;
    private Integer quantity;
    private Timestamp createTime;
    private Timestamp updateTime;
}

package com.summit.stp.payment.infrastructure.persistence.po;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class CommodityPO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Timestamp createTime;
    private Timestamp updateTime;
}

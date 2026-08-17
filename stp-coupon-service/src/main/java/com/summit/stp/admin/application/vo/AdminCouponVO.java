package com.summit.stp.admin.application.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
@Data
@Builder
public class AdminCouponVO implements Serializable {
    private Long id;
    private String name;

    private BigDecimal discount;

    private BigDecimal amount;

    private Integer type;

    private Integer status;

    private Integer scopeType;

    private Integer timeType;

    private Timestamp createTime;

    private Timestamp updateTime;

    private Integer validDays;

    private Integer validHours;

    private String image;

    private String description;

}

package com.summit.stp.admin.api.dto;

import com.summit.stp.coupon.domain.model.Coupon;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class CreateCouponRequest {
    private final String name;
    private final BigDecimal discount;
    private final BigDecimal amount;
    private  int status;
    private Integer type;   // 0 折扣 1金额
    private String description;
    private String image;
    private Integer scopeType;
    private List<Long> scopeRelationIds;
    private Integer timeType;
    private Integer validDays;
    private Integer validHours;
}

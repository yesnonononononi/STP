package com.summit.stp.payment.application.vo;

import com.summit.stp.payment.infrastructure.Enum.CouponStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Builder
public class CouponQueryVO {
    private final Long id;
    private final String name;
    private final BigDecimal discount;
    private final BigDecimal amount;
    private CouponStatus status;
    private final Timestamp createTime;
    private  Timestamp updateTime;
}

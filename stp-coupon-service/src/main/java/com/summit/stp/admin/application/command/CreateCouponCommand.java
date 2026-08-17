package com.summit.stp.admin.application.command;
import com.summit.stp.coupon.domain.model.Coupon;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CreateCouponCommand {
    private final String name;
    private final BigDecimal discount;
    private final BigDecimal amount;
    private  int status;
    private Integer type;   // 0 折扣 1金额
    private String description;
    private String image;
    private final Coupon.CouponScopeType scopeType;
    private List<Long> scopeRelationIds;
    private Coupon.CouponDateType timeType;
    private Integer validDays;
    private Integer validHours;
}

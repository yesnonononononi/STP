package com.summit.stp.member.domain.model;

import com.summit.stp.shared.exception.ParameterException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 会员 (领域模型)
 */
@Getter
@Builder
@EqualsAndHashCode
public class Member {
    private final Long id;     // 会员ID
    private final String name; // 会员名称
    private final MemberType type;
    private final BigDecimal price; // 会员价格
    private final Integer duration; // 会员时长(天数等)
    private final Double discount;  // 会员折扣
    private final String description;
    private final BigDecimal dailyRate;
    private final Integer priority;
    private final Long typeId;
    private final Boolean isSuper;
    private Integer stock;


    /**
     * 价格验证规则
     */
    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ParameterException("会员价格不能小于或等于0");
        }
    }
}

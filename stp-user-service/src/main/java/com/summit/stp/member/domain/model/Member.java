package com.summit.stp.member.domain.model;

import com.summit.stp.common.application.domain.exception.ParameterException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 会员 (领域模型)
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Member {
    private Long id;     // 会员ID
    private String name; // 会员名称
    private MemberType type;
    private BigDecimal price; // 会员价格
    private Integer duration; // 会员时长(天数等)
    private Double discount;  // 会员折扣
    private String description;
    private BigDecimal dailyRate;
    private Integer priority;
    private Long typeId;
    private Boolean isSuper;
    private Integer stock;
    private Instant createTime;

    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ParameterException("会员价格不能小于或等于0");
        }
    }
}

package com.summit.stp.member.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class MemberCreateRequest {
    private String name; // 会员名称
    private String type;
    private BigDecimal price; // 会员价格
    private Integer duration; // 会员时长(天数等)
    private Double discount;  // 会员折扣
    private String description;
    private BigDecimal dailyRate;
    private Integer priority;
    private Long typeId;
    private Boolean isSuper;
    private Integer stock;
}

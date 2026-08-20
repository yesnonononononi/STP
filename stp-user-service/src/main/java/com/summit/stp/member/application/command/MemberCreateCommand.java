package com.summit.stp.member.application.command;

import com.summit.stp.member.domain.model.MemberType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class MemberCreateCommand {
    private Long id;
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
    private Integer status;
}

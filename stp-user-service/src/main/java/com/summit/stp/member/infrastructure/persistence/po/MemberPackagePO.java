package com.summit.stp.member.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("member_package")
public class MemberPackagePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer duration;
    private Instant createTime;
    private Instant updateTime;
    private String description;
    private Long typeId;
    private Integer stock;
    private Double discount;
    private BigDecimal dailyRate;
    private Integer priority;
    private Integer status;
}

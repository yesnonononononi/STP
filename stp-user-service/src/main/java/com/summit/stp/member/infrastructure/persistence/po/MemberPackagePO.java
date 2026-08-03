package com.summit.stp.member.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@TableName("member_package")
public class MemberPackagePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(fill = FieldFill.INSERT)
    private Long publicId;
    private String name;
    private BigDecimal price;
    private Integer duration;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String description;
    private Long typeId;
    private Integer stock;
    private Double discount;
    private BigDecimal dailyRate;
    private Integer priority;
}

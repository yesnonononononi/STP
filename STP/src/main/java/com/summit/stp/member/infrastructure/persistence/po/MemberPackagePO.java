package com.summit.stp.member.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@TableName("member_package")
public class MemberPackagePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer duration;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String description;
    private Long typeId;
    private Double discount;
    private BigDecimal dailyRate;
    private Integer priority;
}

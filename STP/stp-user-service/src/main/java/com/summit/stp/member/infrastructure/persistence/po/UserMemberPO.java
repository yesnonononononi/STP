package com.summit.stp.member.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_member")
public class UserMemberPO {
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;
    private double totalRecharge;
    private long vipLevel;
    private Long packageTypeId;
    private Timestamp levelUpgradeTime;
    private Timestamp updateTime;
    private Timestamp expireTime;
    private BigDecimal dailyRate;
}

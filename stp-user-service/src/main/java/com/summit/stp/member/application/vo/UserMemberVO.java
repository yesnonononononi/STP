package com.summit.stp.member.application.vo;

import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.model.MemberType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@AllArgsConstructor
@Data
@Builder
public class UserMemberVO {
    private long userId;
    private double totalRecharge;
    private MemberLevelConfig level;
    private MemberType memberType;
    private Timestamp levelUpgradeTime;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Timestamp expireTime;
    private Long packageTypeId;
    private BigDecimal dailyRate;
}

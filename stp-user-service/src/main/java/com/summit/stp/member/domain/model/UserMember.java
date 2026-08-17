package com.summit.stp.member.domain.model;

import ch.qos.logback.core.joran.action.TimestampAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserMember {
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

    /**
     * 增加总充值金额(自动管理升级等级)
     * @param amount 充值金额
     */
    public void addTotalRecharge(double amount) {
        this.totalRecharge += amount;
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }

    public boolean isMemberActive() {
        return this.expireTime != null && this.expireTime.getTime() > System.currentTimeMillis();
    }

    /**
     * 动态获取当前实际剩余天数（向上取整，剩1秒也算1天；若无效返回0）
     */
    public long getRealRemainingDuration() {
        if (!isMemberActive()) {
            return 0;
        }
        long diff = this.expireTime.getTime() - System.currentTimeMillis();
        return (long) Math.ceil((double) diff / 86400000.0);
    }

    /**
     * 根据充值类型计算新会员天数,等级,会员类型
     * @param member 当前充值的会员
     */
    public void calculateMemberType(Member member) {
        MemberType type = member.getType();
        long now = System.currentTimeMillis();
        
        if (!isMemberActive() || this.memberType == null) {
            this.memberType = type;
            this.packageTypeId = type.getTypeId();
            this.expireTime = new Timestamp(now + member.getDuration() * 86400000L);
            this.dailyRate = member.getDailyRate();
            return;
        }

        int priority = this.memberType.comparePriority(type);
        long remainingDays = getRealRemainingDuration();
        BigDecimal rateAmount = this.dailyRate.multiply(BigDecimal.valueOf(remainingDays));

        switch (priority) {
            case 0 -> {
                this.expireTime = new Timestamp(this.expireTime.getTime() + member.getDuration() * 86400000L);
                this.dailyRate = member.getDailyRate();
            }
            case 1 -> {
                long newDuration = rateAmount.divide(member.getDailyRate(), RoundingMode.HALF_UP).toBigInteger().longValue();
                long totalDuration = newDuration + member.getDuration();
                this.expireTime = new Timestamp(now + totalDuration * 86400000L);
                this.memberType = type;
                this.packageTypeId = type.getTypeId();
                this.dailyRate = member.getDailyRate();
            }
            case -1 -> {
                BigDecimal newRechargeValue = member.getDailyRate().multiply(BigDecimal.valueOf(member.getDuration()));
                long additionalDuration = newRechargeValue.divide(this.dailyRate, RoundingMode.HALF_UP).toBigInteger().longValue();
                this.expireTime = new Timestamp(this.expireTime.getTime() + additionalDuration * 86400000L);
            }
        }
    }

    private void improveLevel(MemberLevelConfig targetLevel) {
        this.level = targetLevel;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        this.levelUpgradeTime = timestamp;
        this.updateTime = timestamp;
    }

    public void recharge(List<MemberLevelConfig> levelList, Member member, int quantity) {
        calculateMemberType(member);
        this.totalRecharge += member.getPrice().multiply(BigDecimal.valueOf(quantity)).doubleValue();
        MemberLevelConfig memberLevelConfig = this.level.calculateMemberLevel(levelList, totalRecharge);
        improveLevel(memberLevelConfig);
    }

    public void raiseLevel() {
        this.level.raiseLevel();
        this.updateTime = Timestamp.from(Instant.now());
    }

    public void extendExpire(Timestamp expireTime) {
        this.expireTime =  expireTime;
        this.updateTime = Timestamp.from(Instant.now());
    }
}

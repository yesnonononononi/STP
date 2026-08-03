package com.summit.stp.member.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;

@Builder
@Getter
public class UserMember {
    private final long userId;
    private double totalRecharge;
    private MemberLevelConfig level;
    private MemberType memberType;
    private Timestamp levelUpgradeTime;
    private final Timestamp createTime;
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
        
        // 第一次充值（当前没有会员，或会员已过期，或 memberType 为 null）
        if (!isMemberActive() || this.memberType == null) {
            this.memberType = type;
            this.packageTypeId = type.getTypeId();
            this.expireTime = new Timestamp(now + member.getDuration() * 86400000L);
            this.dailyRate = member.getDailyRate();
            return;
        }


        int priority = this.memberType.comparePriority(type);
        long remainingDays = getRealRemainingDuration();
        // 使用当前已持有会员的 dailyRate 计算剩余价值！
        BigDecimal rateAmount = this.dailyRate.multiply(BigDecimal.valueOf(remainingDays));

        // 根据会员优先级,动态折算
        switch (priority) {
            case 0 -> {
                // 等级相同，天数直接累加，更新当前单价为套餐单价
                this.expireTime = new Timestamp(this.expireTime.getTime() + member.getDuration() * 86400000L);
                this.dailyRate = member.getDailyRate();
            }
            // 当前持有会员等级低于新充值会员等级（升级折算）
            case 1 -> {
                // 将低等级会员的剩余价值，折算为高等级会员的天数，并加上新充值天数
                long newDuration = rateAmount.divide(member.getDailyRate(), RoundingMode.HALF_UP).toBigInteger().longValue();
                long totalDuration = newDuration + member.getDuration();
                this.expireTime = new Timestamp(now + totalDuration * 86400000L); //86400000L: 一天的时间换算成毫秒
                this.memberType = type;
                this.packageTypeId = type.getTypeId();
                this.dailyRate = member.getDailyRate(); // 升级为新高等级会员的折算单价
            }
            // 当前持有会员等级高于新充值会员等级（高充低，高等级不降级，低等级折算高等级天数）
            case -1 -> {
                // 将新充值的低等级会员价值，折算为当前高等级会员的天数累加上去
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

    /**
     * 更新用户会员充值信息
     * @param levelList 等级列表
     * @param member 当前充值会员
     * @param quantity 充值数量
     */
    public void recharge(List<MemberLevelConfig> levelList, Member member, int quantity) {
        calculateMemberType(member);
        //更新用户充值后的会员信息

        //(1),先加总金额
        this.totalRecharge += member.getPrice().multiply(BigDecimal.valueOf(quantity)).doubleValue();

        //(2),计算等级
        MemberLevelConfig memberLevelConfig = this.level.calculateMemberLevel(levelList, totalRecharge);

        improveLevel(memberLevelConfig);
    }
}

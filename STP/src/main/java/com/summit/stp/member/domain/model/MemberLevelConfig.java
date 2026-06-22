package com.summit.stp.member.domain.model;

import com.summit.stp.shared.exception.ParameterException;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 会员等级配置 (领域聚合根)
 */


@Getter
@Builder
public class MemberLevelConfig {
    private Long level;
    private  String levelName;
    private double minRecharge;
    private String privilegesJson;
    private String iconUrl;
    private long sortOrder;

    /**
     * 比较等级配置
     * 0: 当前会员等级等于待比较会员等级
     * -1: 当前会员等级大于待比较会员等级
     * 1: 当前会员等级小于待比较会员等级
     * @param other 等级配置
     * @return 0/1/-1
     */
    public int compareTo(MemberLevelConfig other) {
        return this.level.compareTo(other.level);
    }

    /**
     * 更新等级配置业务逻辑
     */
    public void updateConfig(String levelName, double minRecharge, String privilegesJson, String iconUrl, long sortOrder) {
        if (levelName == null || levelName.trim().isEmpty()) {
            throw new ParameterException("等级名称不能为空");
        }
        if (minRecharge < 0) {
            throw new ParameterException("最小充值金额不能为负数");
        }
        this.levelName = levelName;
        this.minRecharge = minRecharge;
        this.privilegesJson = privilegesJson;
        this.iconUrl = iconUrl;
        this.sortOrder = sortOrder;
    }



    /**
     * 计算即将达到的等级
     * @param levelList 等级列表
     * @return 目标等级
     */
    public MemberLevelConfig calculateMemberLevel(List<MemberLevelConfig> levelList, double totalRecharge) {
        if (levelList == null || levelList.isEmpty()) {
            return this;
        }
        return levelList.stream()
                .filter(c -> totalRecharge >= c.getMinRecharge())
                .max(java.util.Comparator.comparing(MemberLevelConfig::getLevel))
                .orElse(this);
    }
}

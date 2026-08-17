package com.summit.stp.member.domain.model;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONUtil;
import com.summit.stp.common.application.domain.exception.ParameterException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 会员等级配置 (领域聚合根)
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberLevelConfig {
    private Long id;
    private Long level;
    private String levelName;
    private double minRecharge;
    private List<Privilege> privileges;
    private String iconUrl;
    private long sortOrder;
    public void raiseLevel(){
        this.level += 1;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Privilege implements Serializable {
        private String name;
        private String description;
        private boolean enabled;

        public static List<Privilege> deserialize(String json, boolean ignoreError) {
            if (StrUtil.isBlank(json)) return new ArrayList<>();
            return JSONUtil.toList(json, Privilege.class);
        }

        public static String serialize(List<Privilege> privilege) {
            if (privilege == null) return "{}";
            return JSONUtil.toJsonStr(privilege);
        }
    }


    public int compareTo(MemberLevelConfig other) {
        return this.level.compareTo(other.level);
    }

    public void updateConfig(String levelName, double minRecharge, String privilegesJson, String iconUrl, long sortOrder) {
        if (levelName == null || levelName.trim().isEmpty()) {
            throw new ParameterException("等级名称不能为空");
        }
        if (minRecharge < 0) {
            throw new ParameterException("最小充值金额不能为负数");
        }
        this.levelName = levelName;
        this.minRecharge = minRecharge;
        if(StrUtil.isNotBlank(privilegesJson))this.privileges = Privilege.deserialize(privilegesJson,false);
        this.iconUrl = iconUrl;
        this.sortOrder = sortOrder;
    }

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

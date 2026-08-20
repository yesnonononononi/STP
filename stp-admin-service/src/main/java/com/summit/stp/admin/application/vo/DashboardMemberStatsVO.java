package com.summit.stp.admin.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMemberStatsVO {

    /**
     * 各会员类型 (MemberType) 下的套餐销量与占比 (Tab 1 & Tab 2)
     */
    private List<TypePackageDistGroup> typePackageGroups;

    /**
     * VIP 等级水平人数占比 (Tab 3)
     */
    private List<LevelDistItem> levelDistribution;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypePackageDistGroup {
        private Long typeId;
        private String typeName;
        private List<PackageDistItem> packages;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackageDistItem {
        private Long packageId;
        private String packageName;
        private Long count;
        private Double salesAmount;
        private Double percentage;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LevelDistItem {
        private Integer level;
        private String levelName;
        private Long count;
        private Double percentage;
    }
}

package com.summit.stp.user.api.vo.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDistributionVO implements Serializable {

    /**
     * 按会员类型 (MemberType) 划分的套餐销售/人数占比
     */
    private List<MemberTypePackageDist> typePackageDistributions;

    /**
     * 按充值金额设定的 VIP 等级人数占比
     */
    private List<MemberLevelDist> levelDistributions;

    // 兼容原简单字段
    private Long normalCount;
    private Long silverCount;
    private Long goldCount;
    private Long diamondCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberTypePackageDist implements Serializable {
        private Long typeId;
        private String typeName;
        private List<PackageItem> packages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackageItem implements Serializable {
        private Long packageId;
        private String packageName;
        private Long count;
        private Double salesAmount;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberLevelDist implements Serializable {
        private Integer level;
        private String levelName;
        private Long count;
        private Double percentage;
    }
}

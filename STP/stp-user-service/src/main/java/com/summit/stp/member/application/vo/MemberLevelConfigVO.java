package com.summit.stp.member.application.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "MemberLevelConfigVO", description = "会员等级配置信息")
public class MemberLevelConfigVO {
    @ApiModelProperty(value = "会员等级数值", example = "1")
    private Long level;

    @ApiModelProperty(value = "等级名称", example = "黄金会员")
    private String levelName;

    @ApiModelProperty(value = "升级所需的最小充值金额", example = "100.00")
    private double minRecharge;

    @ApiModelProperty(value = "特权配置JSON字符串", example = "{\"discount\":0.9}")
    private String privilegesJson;

    @ApiModelProperty(value = "等级图标URL", example = "http://example.com/icon.png")
    private String iconUrl;

    @ApiModelProperty(value = "排序权重", example = "1")
    private long sortOrder;
}

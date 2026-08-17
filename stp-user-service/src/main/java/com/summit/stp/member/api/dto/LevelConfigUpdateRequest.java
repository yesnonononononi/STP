package com.summit.stp.member.api.dto;

import com.summit.stp.common.annotation.PublicId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "LevelConfigUpdateRequest", description = "更新会员等级配置请求参数")
public class LevelConfigUpdateRequest implements Serializable {

    @PublicId
    @ApiModelProperty(value = "配置唯一ID", example = "1001", required = true)
    private Long id;

    @ApiModelProperty(value = "会员等级数值", example = "1", required = true)
    private Long level;

    @ApiModelProperty(value = "等级名称", example = "黄金会员", required = true)
    private String levelName;

    @ApiModelProperty(value = "升级所需的最小充值金额", example = "100.00", required = true)
    private double minRecharge;

    @ApiModelProperty(value = "特权配置JSON字符串", example = "[{\"name\":\"折扣\"}]")
    private String privilegesJson;

    @ApiModelProperty(value = "等级图标URL", example = "http://example.com/icon.png")
    private String iconUrl;

    @ApiModelProperty(value = "排序权重", example = "1")
    private long sortOrder;
}

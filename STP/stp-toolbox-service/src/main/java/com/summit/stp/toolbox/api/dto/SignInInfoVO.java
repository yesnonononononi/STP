package com.summit.stp.toolbox.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@ApiModel(description = "签到打卡状态信息VO")
public class SignInInfoVO {

    @ApiModelProperty("所有打卡日期列表0 未打卡 1 已打卡 按月份排列")
    private List<Integer> checkedDates;

    @ApiModelProperty("连续打卡天数")
    private Integer consecutiveDays;

    @ApiModelProperty("今日是否已打卡")
    private Boolean todayChecked;

    @ApiModelProperty("本月已打卡天数")
    private Integer monthCheckedCount;

    @ApiModelProperty("总打卡天数")
    private Integer checkedDays;
}

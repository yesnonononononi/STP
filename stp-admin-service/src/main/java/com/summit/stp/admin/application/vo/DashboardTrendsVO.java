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
public class DashboardTrendsVO implements DashboardDegradable {
    private List<String> dates;
    private List<Double> gmvList;
    private List<Integer> orderCountList;
    private List<Integer> newUserCountList;

    /**
     * 降级标记：true 表示用户/订单走势远程取数失败，当前为空列表兜底（非真实空数据）
     */
    private Boolean degraded;
}

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
public class DashboardTrendsVO {
    private List<String> dates;
    private List<Double> gmvList;
    private List<Integer> orderCountList;
    private List<Integer> newUserCountList;
}

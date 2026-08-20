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
public class DashboardContentStatsVO {
    private List<String> dates;
    private List<Integer> postCountList;
    private List<Integer> commentCountList;
    private List<Integer> blockedCountList;
}

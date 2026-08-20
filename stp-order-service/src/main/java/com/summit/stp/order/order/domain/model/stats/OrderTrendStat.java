package com.summit.stp.order.order.domain.model.stats;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderTrendStat {
    private List<String> dates;
    private List<Double> gmvList;
    private List<Integer> orderCountList;
}

package com.summit.stp.order.api.vo.stats;

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
public class OrderTrendsVO implements Serializable {
    private List<String> dates;
    private List<Double> gmvList;
    private List<Integer> orderCountList;
}

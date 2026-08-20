package com.summit.stp.order.order.infrastructure.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderOverviewStatDTO {
    private Double totalGmv;
    private Double todayGmv;
    private Double yesterdayGmv;
}

package com.summit.stp.order.order.infrastructure.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrendStatDTO {
    private String dateStr;
    private Double dayGmv;
    private Integer dayCount;
}

package com.summit.stp.order.order.application.vo;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Builder
@Data
public class OrderCreateVO {
    private Long orderId;
    private Timestamp endTime;
}

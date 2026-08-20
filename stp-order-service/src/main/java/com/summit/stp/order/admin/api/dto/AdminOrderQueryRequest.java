package com.summit.stp.order.admin.api.dto;

import lombok.Data;

@Data
public class AdminOrderQueryRequest {
    private Integer page;
    private Integer size;
    private String orderNo;
    private Long userId;
    private Integer orderType;
    private Integer payStatus;
}

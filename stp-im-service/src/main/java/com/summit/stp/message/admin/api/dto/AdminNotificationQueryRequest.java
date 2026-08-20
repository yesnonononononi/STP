package com.summit.stp.message.admin.api.dto;

import lombok.Data;

@Data
public class AdminNotificationQueryRequest {
    private Integer page;
    private Integer size;
    private String keyword;
    private Integer noticeType;
    private Boolean excludeDeleted;
}

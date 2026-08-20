package com.summit.stp.message.admin.application.vo;

import lombok.Builder;
import lombok.Data;
import java.sql.Timestamp;
import java.util.List;

@Builder
@Data
public class AdminNotificationVO {
    private final Long id;
    private final Long fromUserId;
    private final List<String> images;
    private String content;
    private Integer status;
    private final Long associateUser;
    private final Integer type;
    private Timestamp publicTime;
    private final Timestamp createTime;
    private Timestamp updateTime;
}

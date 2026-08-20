package com.summit.stp.message.admin.application.command;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AdminNotificationQueryCommand {
    private Integer page;
    private Integer size;
    private String keyword;
    private Integer noticeType;
    private Boolean excludeDeleted;
}

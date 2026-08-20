package com.summit.stp.message.admin.application.command;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class AdminCreateNotificationCommand {
    private String title;
    private String content;
    private Integer noticeType;
    private Integer targetType;
    private Long targetUserId;
    private List<String> imageUrls;
}

package com.summit.stp.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserReport {
    private Long id;
    private Long reporterId;
    private Long reportedId;
    private String reason;
    private UserReportStatusEnum status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public void ignore() {
        this.status = UserReportStatusEnum.IGNORED;
        this.updateTime = LocalDateTime.now();
    }

    public void process() {
        this.status = UserReportStatusEnum.PROCESSED;
        this.updateTime = LocalDateTime.now();
    }
}

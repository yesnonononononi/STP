package com.summit.stp.user.application.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserReportVO {
    private Long id;
    private Long reporterId;
    private String reporterNick;
    private Long reportedId;
    private String reportedNick;
    private String reason;
    private Integer status;
    private String statusDesc;
    private LocalDateTime createTime;
}

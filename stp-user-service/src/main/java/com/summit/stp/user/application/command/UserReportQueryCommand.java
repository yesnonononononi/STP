package com.summit.stp.user.application.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserReportQueryCommand {
    private Integer status;
    private long page;
    private long pageSize;
}

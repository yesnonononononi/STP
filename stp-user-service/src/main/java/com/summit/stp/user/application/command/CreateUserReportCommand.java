package com.summit.stp.user.application.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateUserReportCommand {
    private Long reportedId;
    private String reason;
}

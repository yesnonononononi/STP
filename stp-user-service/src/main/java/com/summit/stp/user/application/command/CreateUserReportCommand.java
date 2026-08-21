package com.summit.stp.user.application.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CreateUserReportCommand {
    private Long reportedId;
    private String reason;
    private List<String> evidence;
}

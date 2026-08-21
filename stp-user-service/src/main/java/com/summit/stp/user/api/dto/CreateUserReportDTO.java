package com.summit.stp.user.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateUserReportDTO {
    private Long reportedId;
    private String reason;
    private List<String> evidence;
}

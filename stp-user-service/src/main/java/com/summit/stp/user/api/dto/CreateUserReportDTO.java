package com.summit.stp.user.api.dto;

import lombok.Data;

@Data
public class CreateUserReportDTO {
    private Long reportedId;
    private String reason;
}

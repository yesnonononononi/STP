package com.summit.stp.admin.application.command;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class AddAdminCommand {
    private Long uid;
    private Integer order;
}

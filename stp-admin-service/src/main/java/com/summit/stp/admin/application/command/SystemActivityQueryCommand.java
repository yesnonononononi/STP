package com.summit.stp.admin.application.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SystemActivityQueryCommand {
    private String type;
    private String module;
    private long page;
    private long pageSize;
}

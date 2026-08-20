package com.summit.stp.admin.application.command;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateSystemActivityCommand {
    private String type;
    private String module;
    private String title;
    private String content;
    private String targetUrl;
}

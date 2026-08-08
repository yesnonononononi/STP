package com.summit.stp.tag.application.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateTagCommand {
    private String id;
    private String tagName;
    private Integer sort;
    private Integer status;
}

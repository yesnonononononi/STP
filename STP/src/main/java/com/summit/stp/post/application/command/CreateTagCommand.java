package com.summit.stp.post.application.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTagCommand {
    private String tagName;
    private Integer sort;
    private Integer status;
}

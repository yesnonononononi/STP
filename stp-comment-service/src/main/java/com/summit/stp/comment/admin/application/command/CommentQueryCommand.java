package com.summit.stp.comment.admin.application.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentQueryCommand {
    private Integer page;
    private Integer pageSize;
    private String keyword;
    private Integer status;
}

package com.summit.stp.comment.application.command;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class CommentReplyQueryCommand {
    private Long rootId;
    private Long postId;
    private Long cursor;
    private Integer limit;
}

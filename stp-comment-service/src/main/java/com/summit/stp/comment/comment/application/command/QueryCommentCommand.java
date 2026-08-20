package com.summit.stp.comment.comment.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueryCommentCommand {
    private Long idCursor;
    private String hsCursor;
    private Long postId;
    private Integer limit;
}

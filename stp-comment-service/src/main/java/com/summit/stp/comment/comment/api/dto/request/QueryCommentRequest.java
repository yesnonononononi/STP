package com.summit.stp.comment.comment.api.dto.request;

import lombok.Data;

@Data
public class QueryCommentRequest {
    private Long idCursor;
    private String hsCursor;
    private Long postId;
    private Integer limit;
}

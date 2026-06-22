package com.summit.stp.comment.api.dto.request;

import lombok.Data;

@Data
public class CommentReplyQueryRequest {
    private Long cursor;
    private Long postId;
    private Long rootId;
    private Integer limit;
}

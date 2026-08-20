package com.summit.stp.comment.admin.api.request;

import lombok.Data;

@Data
public class CommentQueryRequest {
    private Integer page;
    private Integer pageSize;
    private String keyword;
    private Integer status;
}

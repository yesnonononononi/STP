package com.summit.stp.post.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class QueryPostListByCursorCommand {
    private Long cursor;
    private Boolean self;
    private Long creatorId;
    private Integer status;
    private String orderType;
}

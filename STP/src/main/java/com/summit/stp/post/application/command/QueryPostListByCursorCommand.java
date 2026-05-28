package com.summit.stp.post.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class QueryPostListByCursorCommand {
    public String cursor;
    public Boolean self;
    public Long creatorId;
    public Integer status;
}

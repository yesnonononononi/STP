package com.summit.stp.comment.comment.application.command;

import com.summit.stp.comment.comment.domain.model.Comment;
import com.summit.stp.comment.comment.domain.model.CommentType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateCommentCommand {
    private Long postId;
    private Long rootId;
    private Long parentId;
    private String content;
    private CommentType type;
    private Comment.Extra extra;

}

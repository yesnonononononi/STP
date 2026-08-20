package com.summit.stp.comment.comment.api.dto.request;

import com.summit.stp.comment.comment.domain.model.Comment;
import com.summit.stp.comment.comment.domain.model.CommentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentRequest {
    private Long postId;
    private Long rootId;
    private Long parentId;
    private String content;
    private CommentType type;
    private Comment.Extra extra;
}

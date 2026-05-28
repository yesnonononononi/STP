package com.summit.stp.comment.application.service;

import com.summit.stp.comment.application.command.CreateCommentImageCommand;
import com.summit.stp.comment.application.command.UpdateCommentImageCommand;
import com.summit.stp.comment.application.vo.CommentImageVO;
import java.util.List;

public interface CommentImageAppService {
    CommentImageVO getCommentImageById(Long id);
    List<CommentImageVO> getImagesByCommentId(Long commentId);
    void createCommentImage(CreateCommentImageCommand command);
    void updateCommentImage(UpdateCommentImageCommand command);
    void deleteCommentImage(Long id);
    void deleteImagesByCommentId(Long commentId);
}

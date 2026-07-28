package com.summit.stp.comment.application.service.impl;

import com.summit.stp.comment.application.command.CreateCommentImageCommand;
import com.summit.stp.comment.application.command.UpdateCommentImageCommand;
import com.summit.stp.comment.application.service.CommentImageAppService;
import com.summit.stp.comment.application.vo.CommentImageVO;
import com.summit.stp.comment.domain.repository.CommentImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentImageAppServiceImpl implements CommentImageAppService {
    private final CommentImageRepository commentImageRepository;

    @Override
    public CommentImageVO getCommentImageById(Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<CommentImageVO> getImagesByCommentId(Long commentId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void createCommentImage(CreateCommentImageCommand command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void updateCommentImage(UpdateCommentImageCommand command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deleteCommentImage(Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deleteImagesByCommentId(Long commentId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}

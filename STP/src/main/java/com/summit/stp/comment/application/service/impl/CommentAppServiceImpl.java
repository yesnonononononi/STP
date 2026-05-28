package com.summit.stp.comment.application.service.impl;

import com.summit.stp.comment.application.service.CommentAppService;
import com.summit.stp.comment.domain.repository.CommentRepository;
import com.summit.stp.comment.infrastructure.persistence.po.CommentsPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentAppServiceImpl implements CommentAppService {
    private final CommentRepository commentRepository;

    @Override
    public CommentsPO getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    @Override
    public void postComment(CommentsPO comment) {
        commentRepository.save(comment);
    }

    @Override
    public void deleteComment(Long id) {
        commentRepository.delete(id);
    }

    @Override
    public List<CommentsPO> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }
}

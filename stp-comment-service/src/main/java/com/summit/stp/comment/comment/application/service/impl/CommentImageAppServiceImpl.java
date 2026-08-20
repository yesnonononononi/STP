package com.summit.stp.comment.comment.application.service.impl;

import com.summit.stp.comment.comment.application.command.CreateCommentImageCommand;
import com.summit.stp.comment.comment.application.command.UpdateCommentImageCommand;
import com.summit.stp.comment.comment.application.service.CommentImageAppService;
import com.summit.stp.comment.comment.application.vo.CommentImageVO;
import com.summit.stp.comment.comment.domain.model.CommentImage;
import com.summit.stp.comment.comment.domain.repository.CommentImageRepository;
import com.summit.stp.common.application.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentImageAppServiceImpl implements CommentImageAppService {
    private final CommentImageRepository<CommentImage> commentImageRepository;

    @Override
    public CommentImageVO getCommentImageById(Long id) {
        CommentImage image = commentImageRepository.findById(id).orElse(null);
        return toVO(image);
    }

    @Override
    public List<CommentImageVO> getImagesByCommentId(Long commentId) {
        List<CommentImage> list = commentImageRepository.findByCommentId(commentId);
        return list == null ? List.of() : list.stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCommentImage(CreateCommentImageCommand command) {
        if (command == null) return;
        CommentImage image = CommentImage.builder()
                .commentId(command.getCommentId())
                .imageUrl(command.getImageUrl())
                .width(command.getWidth())
                .height(command.getHeight())
                .size(command.getSize())
                .sortOrder(command.getSortOrder())
                .typeCode(1)
                .build();
        commentImageRepository.save(image);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCommentImage(UpdateCommentImageCommand command) {
        if (command == null || command.getId() == null) return;
        CommentImage existing = commentImageRepository.findById(command.getId())
                .orElseThrow(() -> new BusinessException("评论图片不存在"));
        CommentImage updated = CommentImage.builder()
                .id(existing.getId())
                .commentId(existing.getCommentId())
                .imageName(existing.getImageName())
                .imageUrl(command.getImageUrl() != null ? command.getImageUrl() : existing.getImageUrl())
                .width(command.getWidth() != null ? command.getWidth() : existing.getWidth())
                .height(command.getHeight() != null ? command.getHeight() : existing.getHeight())
                .size(command.getSize() != null ? command.getSize() : existing.getSize())
                .sortOrder(command.getSortOrder() != null ? command.getSortOrder() : existing.getSortOrder())
                .typeCode(command.getStatus() != null ? command.getStatus() : existing.getTypeCode())
                .build();
        commentImageRepository.updateById(updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentImage(Long id) {
        if (id == null) return;
        commentImageRepository.delete(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteImagesByCommentId(Long commentId) {
        if (commentId == null) return;
        commentImageRepository.deleteByCommentId(commentId);
    }

    private CommentImageVO toVO(CommentImage image) {
        if (image == null) return null;
        return CommentImageVO.builder()
                .id(image.getId())
                .commentId(image.getCommentId())
                .imageName(image.getImageName())
                .imageUrl(image.getImageUrl())
                .width(image.getWidth())
                .height(image.getHeight())
                .size(image.getSize())
                .sortOrder(image.getSortOrder())
                .status(image.getTypeCode())
                .build();
    }
}

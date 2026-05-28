package com.summit.stp.comment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.comment.domain.repository.CommentImageRepository;
import com.summit.stp.comment.infrastructure.persistence.mapper.CommentImageMapper;
import com.summit.stp.comment.infrastructure.persistence.po.CommentImagePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentImageRepositoryImpl implements CommentImageRepository {
    private final CommentImageMapper commentImageMapper;

    @Override
    public CommentImagePO findById(Long id) {
        return commentImageMapper.selectById(id);
    }

    @Override
    public List<CommentImagePO> findByCommentId(Long commentId) {
        return commentImageMapper.selectList(
                new LambdaQueryWrapper<CommentImagePO>()
                        .eq(CommentImagePO::getCommentId, commentId)
                        .orderByAsc(CommentImagePO::getSortOrder)
        );
    }

    @Override
    public void save(CommentImagePO commentImage) {
        if (commentImage.getId() == null || commentImage.getId() == 0) {
            commentImageMapper.insert(commentImage);
        } else {
            commentImageMapper.updateById(commentImage);
        }
    }

    @Override
    public void delete(Long id) {
        commentImageMapper.deleteById(id);
    }

    @Override
    public void deleteByCommentId(Long commentId) {
        commentImageMapper.delete(
                new LambdaQueryWrapper<CommentImagePO>()
                        .eq(CommentImagePO::getCommentId, commentId)
        );
    }
}

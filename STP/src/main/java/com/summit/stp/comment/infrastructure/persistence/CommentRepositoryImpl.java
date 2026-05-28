package com.summit.stp.comment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.comment.domain.repository.CommentRepository;
import com.summit.stp.comment.infrastructure.persistence.mapper.CommentsMapper;
import com.summit.stp.comment.infrastructure.persistence.po.CommentsPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {
    private final CommentsMapper commentsMapper;

    @Override
    public CommentsPO findById(Long id) {
        return commentsMapper.selectById(id);
    }

    @Override
    public void save(CommentsPO comment) {
        if (comment.getId() == 0) {
            commentsMapper.insert(comment);
        } else {
            commentsMapper.updateById(comment);
        }
    }

    @Override
    public void delete(Long id) {
        commentsMapper.deleteById(id);
    }

    @Override
    public List<CommentsPO> findByPostId(Long postId) {
        return commentsMapper.selectList(
                new LambdaQueryWrapper<CommentsPO>()
                        .eq(CommentsPO::getPostId, postId)
                        .orderByAsc(CommentsPO::getCreateTime)
        );
    }
}

package com.summit.stp.comment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.comment.domain.model.CommentImage;
import com.summit.stp.comment.domain.repository.CommentImageRepository;
import com.summit.stp.comment.infrastructure.persistence.mapper.CommentImageMapper;
import com.summit.stp.comment.infrastructure.persistence.po.CommentImagePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CommentImageRepositoryImpl implements CommentImageRepository {
    private final CommentImageMapper commentImageMapper;

    @Override
    public CommentImagePO findById(Long id) {
        return commentImageMapper.selectOne(new LambdaQueryWrapper<CommentImagePO>()
                .eq(CommentImagePO::getPublicId, id));
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
        CommentImagePO existing = commentImage.getPublicId() == null ? null : commentImageMapper.selectOne(
                new LambdaQueryWrapper<CommentImagePO>().eq(CommentImagePO::getPublicId, commentImage.getPublicId()));
        if (existing == null) {
            commentImageMapper.insert(commentImage);
            return;
        }
        commentImage.setId(existing.getId());
        commentImage.setPublicId(existing.getPublicId());
        commentImageMapper.updateById(commentImage);
    }

    @Override
    public void delete(Long id) {
        commentImageMapper.delete(new LambdaQueryWrapper<CommentImagePO>()
                .eq(CommentImagePO::getPublicId, id));
    }

    @Override
    public void deleteByCommentId(Long commentId) {
        commentImageMapper.delete(
                new LambdaQueryWrapper<CommentImagePO>()
                        .eq(CommentImagePO::getCommentId, commentId)
        );
    }

    @Override
    public Map<Long, List<CommentImage>> queryByCommentIds(List<Long> commentIds) {
        Map<Long, List<CommentImage>> resultMap = new HashMap<>();
        if (commentIds == null || commentIds.isEmpty()) {
            return resultMap;
        }
        commentIds.forEach(id -> resultMap.put(id, new ArrayList<>()));

        List<CommentImagePO> images = commentImageMapper.selectList(
                new LambdaQueryWrapper<CommentImagePO>()
                        .in(CommentImagePO::getCommentId, commentIds)
                        .eq(CommentImagePO::getStatus, 1)
                        .orderByAsc(CommentImagePO::getSortOrder)
        );

        if (images != null) {
            images.forEach(po -> {
                List<CommentImage> list = resultMap.get(po.getCommentId());
                if (list != null) {
                    list.add(CommentImage.builder()
                            .imageUrl(po.getImageUrl())
                            .width(po.getWidth())
                            .height(po.getHeight())
                            .typeCode(po.getStatus())
                            .build());
                }
            });
        }
        return resultMap;
    }
}

package com.summit.stp.comment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.comment.domain.model.CommentImage;
import com.summit.stp.comment.domain.repository.CommentImageRepository;
import com.summit.stp.comment.infrastructure.persistence.po.CommentImagePO;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CommentImageRepositoryImpl extends AbstractRepository<CommentImagePO, CommentImagePO> implements CommentImageRepository<CommentImagePO> {

    public CommentImageRepositoryImpl(BaseMapper<CommentImagePO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public CommentImagePO findPOById(Long id) {
        return findBy(id, CommentImagePO::getId).orElse(null);
    }

    @Override
    public List<CommentImagePO> findByCommentId(Long commentId) {
        return getBaseMapper().selectList(
                new LambdaQueryWrapper<CommentImagePO>()
                        .eq(CommentImagePO::getCommentId, commentId)
                        .orderByAsc(CommentImagePO::getSortOrder)
        );
    }

    @Override
    public void save(CommentImagePO commentImage) {
        if (commentImage == null) return;
        if (commentImage.getId() != null && findById(commentImage.getId()).isPresent()) {
            super.updateById(commentImage);
        } else {
            super.save(commentImage);
        }
    }

    @Override
    public void delete(Long id) {
        delete(id, CommentImagePO::getId);
    }

    @Override
    public void deleteByCommentId(Long commentId) {
        getBaseMapper().delete(
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

        List<CommentImagePO> images = getBaseMapper().selectList(
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

    @Override
    protected CommentImagePO toPO(CommentImagePO entity) {
        return entity;
    }

    @Override
    protected CommentImagePO toModel(CommentImagePO po) {
        return po;
    }
}


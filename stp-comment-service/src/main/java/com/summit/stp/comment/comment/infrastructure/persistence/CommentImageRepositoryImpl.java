package com.summit.stp.comment.comment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.comment.comment.domain.model.CommentImage;
import com.summit.stp.comment.comment.domain.repository.CommentImageRepository;
import com.summit.stp.comment.comment.infrastructure.persistence.po.CommentImagePO;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CommentImageRepositoryImpl extends AbstractRepository<CommentImage, CommentImagePO> implements CommentImageRepository<CommentImage> {

    public CommentImageRepositoryImpl(BaseMapper<CommentImagePO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public List<CommentImage> findByCommentId(Long commentId) {
        List<CommentImagePO> poList = getBaseMapper().selectList(
                new LambdaQueryWrapper<CommentImagePO>()
                        .eq(CommentImagePO::getCommentId, commentId)
                        .orderByAsc(CommentImagePO::getSortOrder)
        );
        return poList == null ? List.of() : poList.stream().map(this::toModel).toList();
    }



    @Override
    public void updateById(CommentImage commentImage) {
        if (commentImage == null) return;
        super.updateById(commentImage);
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
                    list.add(toModel(po));
                }
            });
        }
        return resultMap;
    }

    @Override
    protected CommentImagePO toPO(CommentImage entity) {
        if (entity == null) return null;
        return CommentImagePO.builder()
                .id(entity.getId())
                .name(entity.getImageName())
                .commentId(entity.getCommentId())
                .imageUrl(entity.getImageUrl())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .size(entity.getSize())
                .sortOrder(entity.getSortOrder())
                .status(entity.getTypeCode() != null ? entity.getTypeCode() : 1)
                .build();
    }

    @Override
    protected CommentImage toModel(CommentImagePO po) {
        if (po == null) return null;
        return CommentImage.builder()
                .id(po.getId())
                .imageName(po.getName())
                .commentId(po.getCommentId())
                .imageUrl(po.getImageUrl())
                .width(po.getWidth())
                .height(po.getHeight())
                .size(po.getSize())
                .sortOrder(po.getSortOrder())
                .typeCode(po.getStatus())
                .build();
    }
}


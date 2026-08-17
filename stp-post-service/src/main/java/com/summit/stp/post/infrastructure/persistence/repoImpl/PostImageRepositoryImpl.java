package com.summit.stp.post.infrastructure.persistence.repoImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.post.application.vo.PostImageVO;
import com.summit.stp.post.domain.model.PostImage;
import com.summit.stp.post.domain.repository.PostImageRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostImageMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostImagePO;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class PostImageRepositoryImpl extends AbstractRepository<PostImage, PostImagePO> implements PostImageRepository {
    private final PostImageMapper postImageMapper;

    public PostImageRepositoryImpl(PostImageMapper postImageMapper) {
        super(postImageMapper);
        this.postImageMapper = postImageMapper;
    }

    @Override
    public PostImageVO findVOById(Long id) {
        PostImagePO postImagePO = getBaseMapper().selectOne(new LambdaQueryWrapper<PostImagePO>()
                .eq(PostImagePO::getId, id));
        return postImagePO == null ? null : convertToVO(postImagePO);
    }

    @Override
    public List<PostImageVO> findByPostId(Long postId) {
        LambdaQueryWrapper<PostImagePO> queryWrapper = new LambdaQueryWrapper<PostImagePO>()
                .eq(PostImagePO::getPostId, postId)
                .orderByAsc(PostImagePO::getSortOrder);
        List<PostImagePO> postImagePOS = getBaseMapper().selectList(queryWrapper);
        return postImagePOS.stream().map(this::convertToVO).toList();
    }

    @Override
    public void save(PostImage postImage) {
        if (postImage == null) return;
        if (postImage.getId() != null && findById(postImage.getId()).isPresent()) {
            super.updateById(postImage);
        } else {
            super.save(postImage);
        }
    }

    @Override
    public void delete(Long id) {
        delete(id, PostImagePO::getId);
    }

    @Override
    public void deleteByPostId(Long postId) {
        delete(postId, PostImagePO::getPostId);
    }

    @Override
    public Long countByPostId(Long postId) {
        return getBaseMapper().selectCount(
                new LambdaQueryWrapper<PostImagePO>()
                        .eq(PostImagePO::getPostId, postId)
        );
    }

    @Override
    public Map<Long, List<PostImageVO>> findByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<PostImageVO> list = postImageMapper.findByIds(postIds);
        return list.stream().collect(Collectors.groupingBy(PostImageVO::getPostId));
    }

    @Override
    public void batchSave(List<PostImage> urls) {
        if (urls == null || urls.isEmpty()) return;
        getBaseMapper().insert(urls.stream().map(this::toPO).toList());
    }

    @Override
    public PostImagePO toPO(PostImage postImage) {
        if (postImage == null) return null;
        return PostImagePO.builder()
                .id(postImage.getId())
                .postId(postImage.getPostId())
                .imageUrl(postImage.getImageUrl())
                .width(postImage.getWidth())
                .height(postImage.getHeight())
                .size(postImage.getSize())
                .sortOrder(postImage.getSortOrder())
                .status(postImage.getStatus() == null ? null : postImage.getStatus().getCode())
                .createTime(postImage.getCreateTime())
                .build();
    }

    @Override
    protected PostImage toModel(PostImagePO po) {
        if (po == null) return null;
        return PostImage.builder()
                .id(po.getId())
                .postId(po.getPostId())
                .imageUrl(po.getImageUrl())
                .width(po.getWidth())
                .height(po.getHeight())
                .size(po.getSize())
                .sortOrder(po.getSortOrder())
                .createTime(po.getCreateTime())
                .build();
    }

    public PostImageVO convertToVO(PostImagePO po) {
        return PostImageVO.builder()
                .id(po.getId())
                .postId(po.getPostId())
                .imageUrl(po.getImageUrl())
                .width(po.getWidth())
                .height(po.getHeight())
                .size(po.getSize())
                .sortOrder(po.getSortOrder())
                .status(po.getStatus())
                .createTime(po.getCreateTime())
                .build();
    }
}


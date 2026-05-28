package com.summit.stp.post.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.post.application.vo.PostImageVO;
import com.summit.stp.post.domain.model.PostImage;
import com.summit.stp.post.domain.repository.PostImageRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostImageMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostImagePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PostImageRepositoryImpl implements PostImageRepository {
    private final PostImageMapper postImageMapper;

    @Override
    public PostImageVO findById(Long id) {
        PostImagePO postImagePO = postImageMapper.selectById(id);
        return postImagePO == null ? null :  convertToVO(postImagePO);
    }

    @Override
    public List<PostImageVO> findByPostId(Long postId) {
        LambdaQueryWrapper<PostImagePO> queryWrapper = new LambdaQueryWrapper<PostImagePO>()
                .eq(PostImagePO::getPostId, postId)
                .orderByAsc(PostImagePO::getSortOrder);
        List<PostImagePO> postImagePOS = postImageMapper.selectList(queryWrapper);
        return postImagePOS.stream().map(this::convertToVO).toList();
    }

    @Override
    public void save(PostImage postImage) {
        if (postImage.getId() == null || postImage.getId() == 0) {
            postImageMapper.insert(convertToPO(postImage));
        } else {
            postImageMapper.updateById(convertToPO(postImage));
        }
    }

    @Override
    public void delete(Long id) {
        postImageMapper.deleteById(id);
    }

    @Override
    public void deleteByPostId(Long postId) {
        postImageMapper.delete(
                new LambdaQueryWrapper<PostImagePO>()
                        .eq(PostImagePO::getPostId, postId)
        );
    }

    @Override
    public Long countByPostId(Long postId) {
        return postImageMapper.selectCount(
                new LambdaQueryWrapper<PostImagePO>()
                        .eq(PostImagePO::getPostId, postId)
        );
    }

    @Override
    public Map<Long, List<PostImageVO>> findByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        List<PostImageVO> list = postImageMapper.findByIds(postIds);
        return list.stream().collect(Collectors.groupingBy(PostImageVO::getPostId));
    }

    @Override
    public void batchSave(List<PostImage> urls) {
        postImageMapper.insert(urls.stream().map(this::convertToPO).toList());
    }

    public PostImagePO convertToPO(PostImage postImage) {
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

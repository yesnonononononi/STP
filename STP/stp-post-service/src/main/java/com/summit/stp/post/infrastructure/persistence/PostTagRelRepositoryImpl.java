package com.summit.stp.post.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.post.domain.model.PostTag;
import com.summit.stp.post.domain.repository.PostTagRelRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostTagRelMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostTagRelPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PostTagRelRepositoryImpl implements PostTagRelRepository {
    private final PostTagRelMapper postTagRelMapper;

    @Override
    public void save(PostTag rel) {
        if (rel == null) {
            return;
        }
        PostTagRelPO po = toPO(rel);
        if (po.getId() == null || po.getId() == 0) {
            postTagRelMapper.insert(po);
        } else {
            postTagRelMapper.updateById(po);
        }
    }

    @Override
    public void delete(Long id) {
        postTagRelMapper.deleteById(id);
    }

    @Override
    public void deleteByPostId(Long postId) {
        postTagRelMapper.delete(
                new LambdaQueryWrapper<PostTagRelPO>().eq(PostTagRelPO::getPostId, postId)
        );
    }

    @Override
    public List<PostTag> findByPostId(Long postId) {
        return postTagRelMapper.selectList(new LambdaQueryWrapper<PostTagRelPO>().eq(PostTagRelPO::getPostId, postId))
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostTag> findByTagId(Long tagId) {
        return postTagRelMapper.selectList(new LambdaQueryWrapper<PostTagRelPO>().eq(PostTagRelPO::getTagId, tagId))
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostTag> findByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }
        return postTagRelMapper.selectList(new LambdaQueryWrapper<PostTagRelPO>().in(PostTagRelPO::getPostId, postIds))
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void batchSave(Long postId, List<Long> tags) {
        List<PostTagRelPO> list = tags.stream().map(tagId -> PostTagRelPO.builder().postId(postId).tagId(tagId).build())
                .toList();
        postTagRelMapper.insert(list);
    }

    private PostTag toDomain(PostTagRelPO po) {
        if (po == null) {
            return null;
        }
        return PostTag.builder()
                .id(po.getId())
                .postId(po.getPostId())
                .tagId(po.getTagId())
                .createTime(po.getCreateTime())
                .build();
    }

    private PostTagRelPO toPO(PostTag domain) {
        if (domain == null) {
            return null;
        }
        return PostTagRelPO.builder()
                .id(domain.getId())
                .postId(domain.getPostId() != null ? domain.getPostId() : 0L)
                .tagId(domain.getTagId() != null ? domain.getTagId() : 0L)
                .createTime(domain.getCreateTime())
                .build();
    }
}

package com.summit.stp.post.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.post.application.vo.PostTagRelVO;
import com.summit.stp.post.domain.repository.PostTagRelRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostTagRelMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostTagRelPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostTagRelRepositoryImpl implements PostTagRelRepository {
    private final PostTagRelMapper postTagRelMapper;

    @Override
    public void save(PostTagRelPO rel) {
        if (rel.getId() == 0) {
            postTagRelMapper.insert(rel);
        } else {
            postTagRelMapper.updateById(rel);
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
    public List<PostTagRelVO> findByPostId(Long postId) {
        return postTagRelMapper.selectList(new LambdaQueryWrapper<PostTagRelPO>().eq(PostTagRelPO::getPostId, postId))
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public List<PostTagRelVO> findByTagId(Long tagId) {
        return postTagRelMapper.selectList(new LambdaQueryWrapper<PostTagRelPO>().eq(PostTagRelPO::getTagId, tagId))
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public List<PostTagRelVO> findByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }
        return postTagRelMapper.selectList(new LambdaQueryWrapper<PostTagRelPO>().in(PostTagRelPO::getPostId, postIds))
                .stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public void batchSave(Long postId, List<Long> tags) {
        List<PostTagRelPO> list = tags.stream().map(tagId -> PostTagRelPO.builder().postId(postId).tagId(tagId).build())
                .toList();
        postTagRelMapper.insert(list);
    }

    private PostTagRelVO convertToVO(PostTagRelPO po) {
        return PostTagRelVO.builder()
                .id(po.getId())
                .postId(po.getPostId())
                .tagId(po.getTagId())
                .build();
    }
}

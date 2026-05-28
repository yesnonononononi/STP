package com.summit.stp.post.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.post.domain.repository.PostCollectRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostCollectMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostCollectPO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostCollectRepositoryImpl implements PostCollectRepository {
    private final PostCollectMapper postCollectMapper;

    @Override
    public void save(PostCollectPO postCollect) {
        if (postCollect.getId() == null || postCollect.getId() == 0) {
            postCollectMapper.insert(postCollect);
        } else {
            postCollectMapper.updateById(postCollect);
        }
    }

    @Override
    public void delete(Long postId, Long userId) {
        postCollectMapper.delete(
                new LambdaQueryWrapper<PostCollectPO>()
                        .eq(PostCollectPO::getPostId, postId)
                        .eq(PostCollectPO::getUserId, userId)
        );
    }

    @Override
    public boolean exists(Long postId, Long userId) {
        return postCollectMapper.selectCount(
                new LambdaQueryWrapper<PostCollectPO>()
                        .eq(PostCollectPO::getPostId, postId)
                        .eq(PostCollectPO::getUserId, userId)
        ) > 0;
    }

    @Override
    public long countByPostId(Long postId) {
        return postCollectMapper.selectCount(
                new LambdaQueryWrapper<PostCollectPO>()
                        .eq(PostCollectPO::getPostId, postId)
        );
    }

    @Override
    public List<Long> findUserIdsByPostId(Long postId) {
        return postCollectMapper.selectList(
                new LambdaQueryWrapper<PostCollectPO>()
                        .select(PostCollectPO::getUserId)
                        .eq(PostCollectPO::getPostId, postId)
        ).stream().map(PostCollectPO::getUserId).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public java.util.Map<Long, java.util.List<Long>> findUserIdsByPostIds(java.util.List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        java.util.List<PostCollectPO> list = postCollectMapper.selectList(
                new LambdaQueryWrapper<PostCollectPO>()
                        .select(PostCollectPO::getPostId, PostCollectPO::getUserId)
                        .in(PostCollectPO::getPostId, postIds)
        );
        return list.stream().collect(java.util.stream.Collectors.groupingBy(
                PostCollectPO::getPostId,
                java.util.stream.Collectors.mapping(PostCollectPO::getUserId, java.util.stream.Collectors.toList())
        ));
    }
}

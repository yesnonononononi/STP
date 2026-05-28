package com.summit.stp.post.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostLikeMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostLikePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostLikeRepositoryImpl implements PostLikeRepository {
    private final PostLikeMapper postLikeMapper;

    @Override
    public void save(PostLikePO postLike) {
        if (postLike.getId() == null || postLike.getId() == 0) {
            postLikeMapper.insert(postLike);
        } else {
            postLikeMapper.updateById(postLike);
        }
    }

    @Override
    public void delete(Long postId, Long userId) {
        postLikeMapper.delete(
                new LambdaQueryWrapper<PostLikePO>()
                        .eq(PostLikePO::getPostId, postId)
                        .eq(PostLikePO::getUserId, userId)
        );
    }

    @Override
    public boolean exists(Long postId, Long userId) {
        return postLikeMapper.selectCount(
                new LambdaQueryWrapper<PostLikePO>()
                        .eq(PostLikePO::getPostId, postId)
                        .eq(PostLikePO::getUserId, userId)
        ) > 0;
    }

    @Override
    public long countByPostId(Long postId) {
        return postLikeMapper.selectCount(
                new LambdaQueryWrapper<PostLikePO>()
                        .eq(PostLikePO::getPostId, postId)
        );
    }

    @Override
    public java.util.List<Long> findUserIdsByPostId(Long postId) {
        return postLikeMapper.selectList(
                new LambdaQueryWrapper<PostLikePO>()
                        .select(PostLikePO::getUserId)
                        .eq(PostLikePO::getPostId, postId)
        ).stream().map(PostLikePO::getUserId).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public java.util.Map<Long, java.util.List<Long>> findUserIdsByPostIds(java.util.List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        java.util.List<PostLikePO> list = postLikeMapper.selectList(
                new LambdaQueryWrapper<PostLikePO>()
                        .select(PostLikePO::getPostId, PostLikePO::getUserId)
                        .in(PostLikePO::getPostId, postIds)
        );
        return list.stream().collect(java.util.stream.Collectors.groupingBy(
                PostLikePO::getPostId,
                java.util.stream.Collectors.mapping(PostLikePO::getUserId, java.util.stream.Collectors.toList())
        ));
    }
}

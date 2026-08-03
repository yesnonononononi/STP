package com.summit.stp.post.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostLikeMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostLikePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<Long> findUserIdsByPostId(Long postId) {
        return postLikeMapper.selectList(
                new LambdaQueryWrapper<PostLikePO>()
                        .select(PostLikePO::getUserId)
                        .eq(PostLikePO::getPostId, postId)
        ).stream().map(PostLikePO::getUserId).collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<Long>> findUserIdsByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<PostLikePO> list = postLikeMapper.selectList(
                new LambdaQueryWrapper<PostLikePO>()
                        .select(PostLikePO::getPostId, PostLikePO::getUserId)
                        .in(PostLikePO::getPostId, postIds)
        );
        return list.stream().collect(Collectors.groupingBy(
                PostLikePO::getPostId,
                Collectors.mapping(PostLikePO::getUserId, Collectors.toList())
        ));
    }

    @Override
    public List<Long> findByUserId(Long userId, String cursor) {
        LambdaQueryWrapper<PostLikePO> eq = new LambdaQueryWrapper<PostLikePO>()
                .eq(PostLikePO::getUserId, userId)
                .orderByDesc(PostLikePO::getPostId)
                .last("limit 10");
        if (cursor != null && !cursor.isEmpty()) {
            eq.lt(PostLikePO::getPostId, cursor);
        }
        return postLikeMapper.selectList(eq).stream().map(PostLikePO::getPostId).collect(Collectors.toList());
    }

    @Override
    public void batchSave(List<PostLikePO> toAddList) {
        postLikeMapper.insert(toAddList);
    }

    @Override
    public void batchDelete(List<Long[]> toRemoveList) {
        if (toRemoveList == null || toRemoveList.isEmpty()) {
            return;
        }
        int batchSize = 100;
        for (int i = 0; i < toRemoveList.size(); i += batchSize) {
            List<Long[]> subList = toRemoveList.subList(i, Math.min(i + batchSize, toRemoveList.size()));
            LambdaQueryWrapper<PostLikePO> queryWrapper = new LambdaQueryWrapper<>();
            for (int j = 0; j < subList.size(); j++) {
                Long[] pair = subList.get(j);
                if (j > 0) {
                    queryWrapper.or();
                }
                queryWrapper.eq(PostLikePO::getPostId, pair[0]).eq(PostLikePO::getUserId, pair[1]);
            }
            postLikeMapper.delete(queryWrapper);
        }
    }
}


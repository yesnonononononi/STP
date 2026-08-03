package com.summit.stp.post.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.post.domain.repository.PostCollectRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostCollectMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostCollectPO;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        ).stream().map(PostCollectPO::getUserId).collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<Long>> findUserIdsByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<PostCollectPO> list = postCollectMapper.selectList(
                new LambdaQueryWrapper<PostCollectPO>()
                        .select(PostCollectPO::getPostId, PostCollectPO::getUserId)
                        .in(PostCollectPO::getPostId, postIds)
        );
        return list.stream().collect(Collectors.groupingBy(
                PostCollectPO::getPostId,
                Collectors.mapping(PostCollectPO::getUserId, Collectors.toList())
        ));
    }

    @Override
    public List<Long> findByUserId(Long userId, String cursor) {
        LambdaQueryWrapper<PostCollectPO> eq = new LambdaQueryWrapper<PostCollectPO>().eq(PostCollectPO::getUserId, userId).last("limit 10");
        if (!StringUtil.isNullOrEmpty(cursor)) {
            eq.le(PostCollectPO::getPostId, cursor);
        }
        return postCollectMapper.selectList(eq).stream().map(PostCollectPO::getPostId).toList();
    }

    @Override
    public void batchSave(List<PostCollectPO> toAddList) {
        postCollectMapper.insert(toAddList);
    }

    @Override
    public void batchDelete(List<Long[]> toRemoveList) {
        if (toRemoveList == null || toRemoveList.isEmpty()) {
            return;
        }
        int batchSize = 100;
        for (int i = 0; i < toRemoveList.size(); i += batchSize) {
            List<Long[]> subList = toRemoveList.subList(i, Math.min(i + batchSize, toRemoveList.size()));
            LambdaQueryWrapper<PostCollectPO> queryWrapper = new LambdaQueryWrapper<>();
            for (int j = 0; j < subList.size(); j++) {
                Long[] pair = subList.get(j);
                if (j > 0) {
                    queryWrapper.or();
                }
                queryWrapper.eq(PostCollectPO::getPostId, pair[0]).eq(PostCollectPO::getUserId, pair[1]);
            }
            postCollectMapper.delete(queryWrapper);
        }
    }
}

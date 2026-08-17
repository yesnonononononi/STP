package com.summit.stp.post.infrastructure.persistence.repoImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.post.domain.repository.PostCollectRepository;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.post.infrastructure.persistence.mapper.PostCollectMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostCollectPO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class PostCollectRepositoryImpl extends AbstractRepository<PostCollectPO, PostCollectPO> implements PostCollectRepository {
    private final PostCollectMapper postCollectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public PostCollectRepositoryImpl(PostCollectMapper postCollectMapper, RedisTemplate<String, Object> redisTemplate) {
        super(postCollectMapper);
        this.postCollectMapper = postCollectMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(PostCollectPO postCollect) {
        if (postCollect == null) return;
        if (postCollect.getId() != null && findById(postCollect.getId()).isPresent()) {
            super.updateById(postCollect);
        } else {
            super.save(postCollect);
        }
    }

    @Override
    public void delete(Long postId, Long userId) {
        getBaseMapper().delete(
                new LambdaQueryWrapper<PostCollectPO>()
                        .eq(PostCollectPO::getPostId, postId)
                        .eq(PostCollectPO::getUserId, userId)
        );
    }

    @Override
    public boolean exists(Long postId, Long userId) {
        return getBaseMapper().selectCount(
                new LambdaQueryWrapper<PostCollectPO>()
                        .eq(PostCollectPO::getPostId, postId)
                        .eq(PostCollectPO::getUserId, userId)
        ) > 0;
    }

    @Override
    public long countByPostId(Long postId) {
        return getBaseMapper().selectCount(
                new LambdaQueryWrapper<PostCollectPO>()
                        .eq(PostCollectPO::getPostId, postId)
        );
    }

    @Override
    public List<Long> findUserIdsByPostId(Long postId) {
        return getBaseMapper().selectList(
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
        List<PostCollectPO> list = getBaseMapper().selectList(
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
        LambdaQueryWrapper<PostCollectPO> eq = new LambdaQueryWrapper<PostCollectPO>()
                .eq(PostCollectPO::getUserId, userId)
                .orderByDesc(PostCollectPO::getPostId)
                .last("limit 10");
        if (cursor != null && !cursor.isEmpty()) {
            try {
                eq.lt(PostCollectPO::getPostId, Long.parseLong(cursor));
            } catch (Exception ignored) {
            }
        }
        List<Long> dbPostIds = getBaseMapper().selectList(eq).stream().map(PostCollectPO::getPostId).toList();
        if (dbPostIds.isEmpty() || userId == null) {
            return dbPostIds;
        }

        List<Long> validPostIds = new ArrayList<>(dbPostIds.size());
        for (Long pid : dbPostIds) {
            String collectKey = PostConstants.Cache.COLLECT_SET_PREFIX + pid;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(collectKey))) {
                Double score = redisTemplate.opsForZSet().score(collectKey, userId);
                if (score != null) {
                    validPostIds.add(pid);
                }
            } else {
                validPostIds.add(pid);
            }
        }
        return validPostIds;
    }

    @Override
    public void batchSave(List<PostCollectPO> toAddList) {
        if (toAddList == null || toAddList.isEmpty()) return;
        getBaseMapper().insert(toAddList);
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
            getBaseMapper().delete(queryWrapper);
        }
    }

    @Override
    protected PostCollectPO toPO(PostCollectPO entity) {
        return entity;
    }

    @Override
    protected PostCollectPO toModel(PostCollectPO po) {
        return po;
    }
}


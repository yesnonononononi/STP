package com.summit.stp.post.infrastructure.persistence.repoImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.post.infrastructure.persistence.mapper.PostLikeMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostLikePO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PostLikeRepositoryImpl extends AbstractRepository<PostLikePO, PostLikePO> implements PostLikeRepository {
    private final PostLikeMapper postLikeMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public PostLikeRepositoryImpl(PostLikeMapper postLikeMapper, RedisTemplate<String, Object> redisTemplate) {
        super(postLikeMapper);
        this.postLikeMapper = postLikeMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(PostLikePO postLike) {
        if (postLike == null) return;
        if (postLike.getId() != null && findById(postLike.getId()).isPresent()) {
            super.updateById(postLike);
        } else {
            super.save(postLike);
        }
    }

    @Override
    public void delete(Long postId, Long userId) {
        getBaseMapper().delete(
                new LambdaQueryWrapper<PostLikePO>()
                        .eq(PostLikePO::getPostId, postId)
                        .eq(PostLikePO::getUserId, userId)
        );
    }

    @Override
    public boolean exists(Long postId, Long userId) {
        return getBaseMapper().selectCount(
                new LambdaQueryWrapper<PostLikePO>()
                        .eq(PostLikePO::getPostId, postId)
                        .eq(PostLikePO::getUserId, userId)
        ) > 0;
    }

    @Override
    public long countByPostId(Long postId) {
        return getBaseMapper().selectCount(
                new LambdaQueryWrapper<PostLikePO>()
                        .eq(PostLikePO::getPostId, postId)
        );
    }

    @Override
    public List<Long> findUserIdsByPostId(Long postId) {
        return getBaseMapper().selectList(
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
        List<PostLikePO> list = getBaseMapper().selectList(
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
            try {
                eq.lt(PostLikePO::getPostId, Long.parseLong(cursor));
            } catch (Exception ignored) {
            }
        }
        List<Long> dbPostIds = getBaseMapper().selectList(eq).stream().map(PostLikePO::getPostId).collect(Collectors.toList());
        if (dbPostIds.isEmpty() || userId == null) {
            return dbPostIds;
        }

        List<Long> validPostIds = new ArrayList<>(dbPostIds.size());
        for (Long pid : dbPostIds) {
            String likeKey = PostConstants.Cache.LIKE_SET_PREFIX + pid;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(likeKey))) {
                Double score = redisTemplate.opsForZSet().score(likeKey, userId);
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
    public void batchSave(List<PostLikePO> toAddList) {
        if (toAddList == null || toAddList.isEmpty()) return;
        getBaseMapper().insert(toAddList);
    }

    @Override
    public void batchSave(Map<Long, Set<Long>> map) {
        if (map == null || map.isEmpty()) return;
        List<PostLikePO> poList = new ArrayList<>();
        map.forEach((postId, set) -> {
            set.forEach(userId -> {
                PostLikePO entity = PostLikePO.builder()
                        .postId(postId)
                        .userId(userId)
                        .build();
                poList.add(entity);
            });
        });
        getBaseMapper().insert(poList);
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
            getBaseMapper().delete(queryWrapper);
        }
    }

    @Override
    protected PostLikePO toPO(PostLikePO entity) {
        return entity;
    }

    @Override
    protected PostLikePO toModel(PostLikePO po) {
        return po;
    }
}



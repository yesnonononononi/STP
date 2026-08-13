package com.summit.stp.post.infrastructure.persistence.repoImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostLikeMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostLikePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.summit.stp.post.infrastructure.constants.PostConstants;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PostLikeRepositoryImpl implements PostLikeRepository {
    private final PostLikeMapper postLikeMapper;
    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public void save(PostLikePO postLike) {
        PostLikePO existing = findExisting(postLike);
        if (existing == null) {
            postLikeMapper.insert(postLike);
            return;
        }
        postLike.setId(existing.getId());
        postLike.setPublicId(existing.getPublicId());
        postLikeMapper.updateById(postLike);
    }

    private PostLikePO findExisting(PostLikePO postLike) {
        if (postLike.getPublicId() != null) {
            return postLikeMapper.selectOne(new LambdaQueryWrapper<PostLikePO>()
                    .eq(PostLikePO::getPublicId, postLike.getPublicId()));
        }
        if (postLike.getId() != null && postLike.getId() != 0) {
            return postLike;
        }
        return null;
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
            try {
                eq.lt(PostLikePO::getPostId, Long.parseLong(cursor));
            } catch (Exception ignored) {
            }
        }
        List<Long> dbPostIds = postLikeMapper.selectList(eq).stream().map(PostLikePO::getPostId).collect(Collectors.toList());
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
        postLikeMapper.insert(toAddList);
    }

    @Override
    public void batchSave(Map<Long, Set<Long>> map){
       List<PostLikePO> poList = new ArrayList<>();
      map.forEach((postId,set)->{
          set.forEach(userId -> {
              PostLikePO entity = PostLikePO.builder()
                      .postId(postId)
                      .userId(userId)
                      .build();
              poList.add(entity);
          });
      });
      postLikeMapper.insert(poList);
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


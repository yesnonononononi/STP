package com.summit.stp.user.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.user.domain.repository.UserFollowRepository;
import com.summit.stp.user.infrastructure.persistence.mapper.UserFollowMapper;
import com.summit.stp.user.infrastructure.persistence.po.UserFollowPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserFollowRepositoryImpl implements UserFollowRepository {
    private final UserFollowMapper userFollowMapper;

    @Override
    public UserFollowPO findById(Long id) {
        return userFollowMapper.selectById(id);
    }

    @Override
    public UserFollowPO findByFollowerAndFollowee(Long followerId, Long followeeId) {
        return userFollowMapper.selectOne(
                new LambdaQueryWrapper<UserFollowPO>()
                        .eq(UserFollowPO::getFollowerId, followerId)
                        .eq(UserFollowPO::getFolloweeId, followeeId)
        );
    }

    @Override
    public java.util.List<UserFollowPO> findByFollowerAndFollowees(Long followerId, java.util.Collection<Long> followeeIds) {
        if (followerId == null || followeeIds == null || followeeIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return userFollowMapper.selectList(
                new LambdaQueryWrapper<UserFollowPO>()
                        .eq(UserFollowPO::getFollowerId, followerId)
                        .in(UserFollowPO::getFolloweeId, followeeIds)
                        .eq(UserFollowPO::getStatus, 1) // 正常关注
        );
    }

    @Override
    public void save(UserFollowPO userFollow) {
        if (userFollow.getId() == null || userFollow.getId() == 0) {
            userFollowMapper.insert(userFollow);
        } else {
            userFollowMapper.updateById(userFollow);
        }
    }

    @Override
    public void delete(Long id) {
        userFollowMapper.deleteById(id);
    }

    @Override
    public Page<UserFollowPO> queryFollowersPage(Long followeeId, long page, long pageSize) {
        return userFollowMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<UserFollowPO>()
                        .eq(UserFollowPO::getFolloweeId, followeeId)
                        .eq(UserFollowPO::getStatus, 1) // 正常关注
                        .orderByDesc(UserFollowPO::getCreateTime)
        );
    }

    @Override
    public Page<UserFollowPO> queryFolloweesPage(Long followerId, long page, long pageSize) {
        return userFollowMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<UserFollowPO>()
                        .eq(UserFollowPO::getFollowerId, followerId)
                        .eq(UserFollowPO::getStatus, 1) // 正常关注
                        .orderByDesc(UserFollowPO::getCreateTime)
        );
    }
}

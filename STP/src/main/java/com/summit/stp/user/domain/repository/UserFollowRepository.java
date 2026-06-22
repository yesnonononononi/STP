package com.summit.stp.user.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.user.infrastructure.persistence.po.UserFollowPO;

public interface UserFollowRepository {
    UserFollowPO findById(Long id);
    UserFollowPO findByFollowerAndFollowee(Long followerId, Long followeeId);
    java.util.List<UserFollowPO> findByFollowerAndFollowees(Long followerId, java.util.Collection<Long> followeeIds);
    void save(UserFollowPO userFollow);
    void delete(Long id);
    Page<UserFollowPO> queryFollowersPage(Long followeeId, long page, long pageSize);
    Page<UserFollowPO> queryFolloweesPage(Long followerId, long page, long pageSize);
}

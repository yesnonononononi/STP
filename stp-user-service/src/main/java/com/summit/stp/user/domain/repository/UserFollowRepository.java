package com.summit.stp.user.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserFollowRepository<T> {
    Optional<T> findById(Long id);
    Optional<T> findByFollowerAndFollowee(Long followerId, Long followeeId);
    List<T> findByFollowerAndFollowees(Long followerId, Collection<Long> followeeIds);
    void save(T userFollow);
    void delete(Long id);
    Page<T> queryFollowersPage(Long followeeId, long page, long pageSize);
    Page<T> queryFolloweesPage(Long followerId, long page, long pageSize);

    void updateById(T existing);
}

package com.summit.stp.user.domain.repository;

import com.summit.stp.user.api.vo.stats.UserTrendsVO;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface UserRepository<T> {
    Long saveUser(T user);

    Optional<T> findUserByName(String username);

    Optional<T> findUserById(Long id);

    Map<Long, T> findUserByIds(Collection<Long> userIds);

    Optional<T> findUserByPhone(String phone);

    void updateById(T user);

    Long countNewUser();

    long countTotalUsers();

    long countUsersCreatedAfter(Instant startTime);

    com.summit.stp.user.domain.model.stats.UserTrendStat countUserTrends(int days);
}

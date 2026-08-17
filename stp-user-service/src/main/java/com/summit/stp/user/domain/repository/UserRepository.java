package com.summit.stp.user.domain.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface UserRepository<T> {
    public Long saveUser(T user);

    Optional<T> findUserByName(String username);

    Optional<T> findUserById(Long id);


    Map<Long, T> findUserByIds(Collection<Long> userIds);

    Optional<T> findUserByPhone(String phone);

    void updateById(T user);
}

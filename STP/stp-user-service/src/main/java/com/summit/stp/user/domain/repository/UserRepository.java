package com.summit.stp.user.domain.repository;

import com.summit.stp.user.domain.model.User;

import java.util.Collection;
import java.util.Map;

public interface UserRepository {
    public void save(User user);

    void put(User user);

    User findUserByName(String username);

    User findUserById(Long id);

    void updateProfile(User user);

    Map<Long, User> findUserByIds(Collection<Long> userIds);
}

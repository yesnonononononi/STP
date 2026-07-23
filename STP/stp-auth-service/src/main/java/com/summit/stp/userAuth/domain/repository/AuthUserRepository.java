package com.summit.stp.userAuth.domain.repository;

import com.summit.stp.userAuth.domain.model.AuthUser;

import java.util.Optional;

public interface AuthUserRepository {
    Optional<AuthUser> findByUsername(String username);
    Optional<AuthUser> findByPhone(String phone);
    void save(AuthUser user);
    boolean existsByPhone(String phone);
}

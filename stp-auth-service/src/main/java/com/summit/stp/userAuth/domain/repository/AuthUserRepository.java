package com.summit.stp.userAuth.domain.repository;

import com.summit.stp.user.api.vo.UserProfileVO;
import com.summit.stp.userAuth.domain.model.AuthUser;

import java.util.Optional;
import java.util.function.Supplier;

public interface AuthUserRepository {
    <T extends Throwable> AuthUser findUserByOrThrow(String username, Long userId, String phone, Supplier<? extends T> error) throws T;

    void save(AuthUser user);

    void updateUserIp(Long userId, String ipLocation);

}

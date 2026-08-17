package com.summit.stp.common.application.domain.repository;

import com.summit.stp.common.application.domain.model.UserSession;

public interface JwtRepository {
    String generateToken(UserSession userSession);

    // 解析 token，返回 Claims
    UserSession parseToken(String token);
}

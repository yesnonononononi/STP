package com.summit.stp.message.message.domain.repository;

import com.summit.stp.message.message.domain.model.UserSession;

import java.util.List;

public interface UserSessionRepository {
    UserSession findByUserIdAndSessionId(Long userId, Long sessionId);

    UserSession findTargetSession(Long sessionId, Long userId);

    List<UserSession> findByUserId(Long userId);

    void save(UserSession userSession);

    void batchUpdate(List<UserSession> list);
}

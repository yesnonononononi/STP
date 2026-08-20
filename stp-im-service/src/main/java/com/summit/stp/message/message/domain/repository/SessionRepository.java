package com.summit.stp.message.message.domain.repository;

import com.summit.stp.message.message.domain.model.Session;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SessionRepository {
    Optional<Session> findById(Long sessionId);

    void save(Session session);

    Map<Long, Session> findByIds(List<Long> list);
}

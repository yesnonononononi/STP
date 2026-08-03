package com.summit.stp.message.domain.repository;

import com.summit.stp.message.domain.model.Session;

import java.util.List;
import java.util.Map;

public interface SessionRepository {
    Session findById(Long sessionId);

    void save(Session session);

    Map<Long, Session> findByIds(List<Long> list);
}

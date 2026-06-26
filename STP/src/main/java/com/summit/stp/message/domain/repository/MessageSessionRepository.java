package com.summit.stp.message.domain.repository;

import com.summit.stp.message.domain.model.MessageSession;

import java.util.List;

public interface MessageSessionRepository {
    MessageSession findById(Long sessionId);


    void save(MessageSession session);

    List<MessageSession> findSessionByUserId(Long id);

    void batchUpdate(List<MessageSession> list);
}

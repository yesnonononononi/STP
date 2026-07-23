package com.summit.stp.message.domain.repository;

import com.summit.stp.message.domain.model.PrivateMessage;

import java.util.List;

public interface PrivateMessageRepository {
    void update(PrivateMessage privateMessage );
    PrivateMessage findById(Long messageId);

    void save(PrivateMessage domain);

    List<PrivateMessage> findMessagesBySessionId(Long sessionId);
    public void batchUpdate(List<PrivateMessage> lists);
}

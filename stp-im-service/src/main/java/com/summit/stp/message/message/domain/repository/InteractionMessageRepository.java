package com.summit.stp.message.message.domain.repository;

import com.summit.stp.message.message.domain.model.InteractionMessage;

import java.util.List;

public interface InteractionMessageRepository {
    void save(InteractionMessage message);
    List<InteractionMessage> getInteractionMessages(Long receiverId, Long lastPublicId, int limit);
    void deleteByUuid(Long id, Long receiverId);
}

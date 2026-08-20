package com.summit.stp.message.message.application.service;

import com.summit.stp.message.message.application.vo.InteractionMessageVO;
import com.summit.stp.message.message.domain.model.InteractionMessage;

import java.util.List;

public interface InteractionMessageService {
    List<InteractionMessageVO> getInteractionList(Long lastPublicId, Integer limit);
    void saveMessage(InteractionMessage msg);
    void deleteInteractionMessage(Long id);
}

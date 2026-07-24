package com.summit.stp.message.application.service.impl;

import com.summit.stp.message.application.service.InteractionMessageService;
import com.summit.stp.message.application.vo.InteractionMessageVO;
import com.summit.stp.message.domain.model.InteractionMessage;
import com.summit.stp.message.domain.repository.InteractionMessageRepository;
import com.summit.stp.shared.ThreadContext.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionMessageServiceImpl implements InteractionMessageService {

    private final InteractionMessageRepository interactionMessageRepository;

    @Override
    public void saveMessage(InteractionMessage msg) {
        interactionMessageRepository.save(msg);
    }

    @Override
    public List<InteractionMessageVO> getInteractionList(Long lastPublicId, Integer limit) {
        Long currentUserId = UserHolder.getUser().getId();
        List<InteractionMessage> messages = interactionMessageRepository.getInteractionMessages(currentUserId, lastPublicId, limit);
        if (messages.isEmpty()) {
            return List.of();
        }

        List<InteractionMessageVO> voList = new ArrayList<>(messages.size());
        for (InteractionMessage msg : messages) {
            InteractionMessageVO vo = buildVo(msg, msg.getAssociateTitle(), msg.getPostId());
            voList.add(vo);
        }

        return voList;
    }

    private InteractionMessageVO buildVo(InteractionMessage msg, String associateContentTitle, Long realPostId) {
        return InteractionMessageVO.builder()
                .publicId(msg.getPublicId())
                .senderId(msg.getSenderId())
                .senderAvatar(msg.getSenderAvatar())
                .senderName(msg.getSenderName())
                .receiverId(msg.getReceiverId())
                .messageType(msg.getMessageType())
                .content(msg.getContent())
                .associateContent(msg.getAssociateContent())
                .postId(realPostId)
                .createTime(msg.getCreateTime())
                .associateContentTitle(associateContentTitle)
                .build();
    }

    @Override
    public void deleteInteractionMessage(Long publicId) {
        Long currentUserId = UserHolder.getUser().getId();
        interactionMessageRepository.deleteByUuid(publicId, currentUserId);
    }
}

package com.summit.stp.message.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.message.domain.model.MessageSession;
import com.summit.stp.message.domain.repository.MessageSessionRepository;
import com.summit.stp.message.infrastructure.persistence.mapper.MessageSessionMapper;
import com.summit.stp.message.infrastructure.persistence.po.MessageSessionPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageSessionRepositoryImpl implements MessageSessionRepository {
    private final MessageSessionMapper messageSessionMapper;

    public MessageSessionRepositoryImpl(MessageSessionMapper messageSessionMapper) {
        this.messageSessionMapper = messageSessionMapper;
    }

    public MessageSessionPO toPO(MessageSession session){
        return MessageSessionPO.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .targetId(session.getTargetId())
                .type(session.getType().getValue())
                .lastMessageId(session.getLastMessageId())
                .lastMessageContent(session.getLastMessageContent())
                .lastSenderId(session.getLastSenderId())
                .lastTime(session.getLastTime())
                .unreadCountForTarget(session.getUnreadCountForTarget())
                .unreadCountForUser(session.getUnreadCountForUser())
                .isTop(session.getIsTop())
                .isMute(session.getIsMute())
                .draft(session.getDraft())
                .isHidden(session.getIsHidden())
                .targetNickName(session.getTargetNickName())
                .targetAvatar(session.getTargetAvatar())
                .createTime(session.getCreateTime())
                .updateTime(session.getUpdateTime())
                .build();
    }

    public MessageSession toDomain(MessageSessionPO po){
        return MessageSession.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .targetId(po.getTargetId())
                .type(MessageSession.Type.fromValue(po.getType()))
                .lastMessageId(po.getLastMessageId())
                .lastMessageContent(po.getLastMessageContent())
                .lastSenderId(po.getLastSenderId())
                .lastTime(po.getLastTime())
                .unreadCountForTarget(po.getUnreadCountForTarget())
                .unreadCountForUser(po.getUnreadCountForUser())
                .isTop(po.getIsTop())
                .isMute(po.getIsMute())
                .draft(po.getDraft())
                .isHidden(po.getIsHidden())
                .targetNickName(po.getTargetNickName())
                .targetAvatar(po.getTargetAvatar())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    @Override
    public MessageSession findById(Long sessionId) {
        MessageSessionPO messageSessionPO = messageSessionMapper.selectById(sessionId);
        if (messageSessionPO == null) {
            return null;
        }
        return toDomain(messageSessionPO);
    }


    @Override
    public void save(MessageSession session) {
        messageSessionMapper.insertOrUpdate(toPO(session));
    }

    @Override
    public List<MessageSession> findSessionByUserId(Long id) {
        LambdaQueryWrapper<MessageSessionPO> queryWrapper = new LambdaQueryWrapper<MessageSessionPO>()
                .eq(MessageSessionPO::getUserId, id)
                .or()
                .eq(MessageSessionPO::getTargetId, id);
        List<MessageSessionPO> messageSessionPOS = messageSessionMapper.selectList(queryWrapper);
        return messageSessionPOS.stream().map(this::toDomain).toList();
    }

    @Override
    public void batchUpdate(List<MessageSession> list) {
        messageSessionMapper.updateById(list.stream().map(this::toPO).toList());
    }
}

package com.summit.stp.message.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.message.domain.model.PrivateMessage;
import com.summit.stp.message.domain.repository.PrivateMessageRepository;
import com.summit.stp.message.infrastructure.persistence.po.PrivateMessagePO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PrivateMessageRepositoryImpl extends AbstractRepository<PrivateMessage, PrivateMessagePO> implements PrivateMessageRepository {

    public PrivateMessageRepositoryImpl(BaseMapper<PrivateMessagePO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public void save(PrivateMessage domain) {
        if (domain == null) return;
        if (domain.getId() != null && findById(domain.getId()).isPresent()) {
            super.updateById(domain);
        } else {
            super.save(domain);
        }
    }

    @Override
    public List<PrivateMessage> findMessagesBySessionId(Long sessionId) {
        return findListBy(sessionId, PrivateMessagePO::getSessionId);
    }

    @Override
    public void update(PrivateMessage privateMessage) {
        if (privateMessage == null) return;
        updateById(privateMessage);
    }

    public List<PrivateMessage> findHistory(Long sessionId, Long cursorId, Integer limit) {
        LambdaQueryWrapper<PrivateMessagePO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PrivateMessagePO::getSessionId, sessionId);
        queryWrapper.ne(PrivateMessagePO::getStatus, PrivateMessage.Status.WITHDRAWN.getValue());

        if (cursorId != null) {
            queryWrapper.lt(PrivateMessagePO::getId, cursorId);
        }
        queryWrapper.orderByDesc(PrivateMessagePO::getId).last("limit " + limit);

        List<PrivateMessagePO> privateMessagePOS = getBaseMapper().selectList(queryWrapper);
        return privateMessagePOS.stream().map(this::toModel).toList();
    }

    @Override
    public void batchUpdate(List<PrivateMessage> lists) {
        if (lists == null || lists.isEmpty()) return;
        update(lists);
    }

    @Override
    public PrivateMessagePO toPO(PrivateMessage privateMessage) {
        if (privateMessage == null) return null;
        return PrivateMessagePO.builder()
                .id(privateMessage.getId())
                .userId(privateMessage.getUserId())
                .receiverId(privateMessage.getReceiverId())
                .content(privateMessage.getContent())
                .image(privateMessage.getImage())
                .audio(privateMessage.getAudio())
                .video(privateMessage.getVideo())
                .type(privateMessage.getType() != null ? privateMessage.getType().getValue() : null)
                .sessionId(privateMessage.getSessionId())
                .status(privateMessage.getStatus() != null ? privateMessage.getStatus().getValue() : null)
                .sendTime(privateMessage.getSendTime())
                .build();
    }

    @Override
    public PrivateMessage toModel(PrivateMessagePO privateMessagePO) {
        if (privateMessagePO == null) return null;
        return PrivateMessage.builder()
                .id(privateMessagePO.getId())
                .userId(privateMessagePO.getUserId())
                .receiverId(privateMessagePO.getReceiverId())
                .content(privateMessagePO.getContent())
                .image(privateMessagePO.getImage())
                .audio(privateMessagePO.getAudio())
                .video(privateMessagePO.getVideo())
                .sessionId(privateMessagePO.getSessionId())
                .status(privateMessagePO.getStatus() != null ? PrivateMessage.Status.fromValue(privateMessagePO.getStatus()) : null)
                .sendTime(privateMessagePO.getSendTime())
                .type(privateMessagePO.getType() != null ? PrivateMessage.Type.fromValue(privateMessagePO.getType()) : null)
                .build();
    }
}


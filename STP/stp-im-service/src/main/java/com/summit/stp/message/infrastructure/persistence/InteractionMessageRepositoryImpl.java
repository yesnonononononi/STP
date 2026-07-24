package com.summit.stp.message.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.message.domain.model.InteractionMessage;
import com.summit.stp.message.domain.repository.InteractionMessageRepository;
import com.summit.stp.message.infrastructure.persistence.mapper.InteractionMessageMapper;
import com.summit.stp.message.infrastructure.persistence.po.InteractionMessagePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class InteractionMessageRepositoryImpl implements InteractionMessageRepository {
    private final InteractionMessageMapper interactionMessageMapper;

    private InteractionMessagePO toPO(InteractionMessage message) {
        return InteractionMessagePO.builder()
                .id(message.getId())
                .publicId(message.getPublicId())
                .senderId(message.getSenderId())
                .senderAvatar(message.getSenderAvatar())
                .senderName(message.getSenderName())
                .receiverId(message.getReceiverId())
                .messageType(message.getMessageType())
                .content(message.getContent())
                .associateContent(message.getAssociateContent())
                .postId(message.getPostId())
                .associateTitle(message.getAssociateTitle())
                .isDel(message.getIsDel() != null ? message.getIsDel() : 0)
                .createTime(message.getCreateTime())
                .updateTime(message.getUpdateTime())
                .build();
    }

    private InteractionMessage toDomain(InteractionMessagePO po) {
        return InteractionMessage.builder()
                .id(po.getId())
                .publicId(po.getPublicId())
                .senderId(po.getSenderId())
                .senderAvatar(po.getSenderAvatar())
                .senderName(po.getSenderName())
                .receiverId(po.getReceiverId())
                .messageType(po.getMessageType())
                .content(po.getContent())
                .associateContent(po.getAssociateContent())
                .postId(po.getPostId())
                .associateTitle(po.getAssociateTitle())
                .isDel(po.getIsDel())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    @Override
    public void save(InteractionMessage message) {
        InteractionMessagePO po = toPO(message);
        if (po.getCreateTime() == null) {
            po.setCreateTime(Instant.now());
        }
        if (po.getUpdateTime() == null) {
            po.setUpdateTime(Instant.now());
        }
        interactionMessageMapper.insert(po);
    }

    @Override
    public List<InteractionMessage> getInteractionMessages(Long receiverId, Long lastPublicId, int limit) {
        Page<InteractionMessagePO> p = new Page<>(1, limit);
        LambdaQueryWrapper<InteractionMessagePO> queryWrapper = new LambdaQueryWrapper<InteractionMessagePO>()
                .eq(InteractionMessagePO::getReceiverId, receiverId)
                .eq(InteractionMessagePO::getIsDel, 0);

        if (lastPublicId != null && lastPublicId > 0) {
            queryWrapper.lt(InteractionMessagePO::getPublicId, lastPublicId);
        }

        queryWrapper.orderByDesc(InteractionMessagePO::getPublicId);
        Page<InteractionMessagePO> pages = interactionMessageMapper.selectPage(p, queryWrapper);
        return pages.getRecords().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteByUuid(Long publicId, Long receiverId) {
        LambdaQueryWrapper<InteractionMessagePO> wrapper = new LambdaQueryWrapper<InteractionMessagePO>()
                .eq(InteractionMessagePO::getPublicId, publicId)
                .eq(InteractionMessagePO::getReceiverId, receiverId);

        InteractionMessagePO po = new InteractionMessagePO();
        po.setIsDel(1);
        interactionMessageMapper.update(po, wrapper);
    }
}

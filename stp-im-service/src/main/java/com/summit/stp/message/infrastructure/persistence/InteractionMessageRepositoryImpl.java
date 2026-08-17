package com.summit.stp.message.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.message.domain.model.InteractionMessage;
import com.summit.stp.message.domain.repository.InteractionMessageRepository;
import com.summit.stp.message.infrastructure.persistence.po.InteractionMessagePO;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class InteractionMessageRepositoryImpl extends AbstractRepository<InteractionMessage, InteractionMessagePO> implements InteractionMessageRepository {

    public InteractionMessageRepositoryImpl(BaseMapper<InteractionMessagePO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public void save(InteractionMessage message) {
        if (message == null) return;
        InteractionMessagePO po = toPO(message);
        if (po.getCreateTime() == null) {
            po.setCreateTime(Instant.now());
        }
        if (po.getUpdateTime() == null) {
            po.setUpdateTime(Instant.now());
        }
        getBaseMapper().insert(po);
    }

    @Override
    public List<InteractionMessage> getInteractionMessages(Long receiverId, Long lastPublicId, int limit) {
        Page<InteractionMessagePO> p = new Page<>(1, limit);
        LambdaQueryWrapper<InteractionMessagePO> queryWrapper = new LambdaQueryWrapper<InteractionMessagePO>()
                .eq(InteractionMessagePO::getReceiverId, receiverId)
                .eq(InteractionMessagePO::getIsDel, 0);

        if (lastPublicId != null && lastPublicId > 0) {
            queryWrapper.lt(InteractionMessagePO::getId, lastPublicId);
        }

        queryWrapper.orderByDesc(InteractionMessagePO::getId);
        Page<InteractionMessagePO> pages = getBaseMapper().selectPage(p, queryWrapper);
        return pages.getRecords().stream().map(this::toModel).toList();
    }

    @Override
    public void deleteByUuid(Long id, Long receiverId) {
        LambdaQueryWrapper<InteractionMessagePO> wrapper = new LambdaQueryWrapper<InteractionMessagePO>()
                .eq(InteractionMessagePO::getId, id)
                .eq(InteractionMessagePO::getReceiverId, receiverId);

        InteractionMessagePO po = new InteractionMessagePO();
        po.setIsDel(1);
        getBaseMapper().update(po, wrapper);
    }

    @Override
    protected InteractionMessagePO toPO(InteractionMessage message) {
        if (message == null) return null;
        return InteractionMessagePO.builder()
                .id(message.getId())
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

    @Override
    protected InteractionMessage toModel(InteractionMessagePO po) {
        if (po == null) return null;
        return InteractionMessage.builder()
                .id(po.getId())
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
}


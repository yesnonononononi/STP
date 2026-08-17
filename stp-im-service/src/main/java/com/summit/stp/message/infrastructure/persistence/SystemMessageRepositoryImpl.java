package com.summit.stp.message.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.message.domain.model.SystemMessage;
import com.summit.stp.message.domain.repository.SystemMessageRepository;
import com.summit.stp.message.infrastructure.persistence.po.SystemMessagePO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SystemMessageRepositoryImpl extends AbstractRepository<SystemMessage, SystemMessagePO> implements SystemMessageRepository {

    public SystemMessageRepositoryImpl(BaseMapper<SystemMessagePO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public List<SystemMessage> list(Long userId, Integer page, Integer pageSize) {
        Page<SystemMessagePO> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<SystemMessagePO> queryWrapper = new LambdaQueryWrapper<SystemMessagePO>()
                .and(wrapper -> wrapper.eq(SystemMessagePO::getType, 1)
                        .or(w -> w.eq(SystemMessagePO::getAssociateUser, userId)))
                .orderByDesc(SystemMessagePO::getCreateTime);
        Page<SystemMessagePO> pages = getBaseMapper().selectPage(p, queryWrapper);
        return pages.getRecords().stream().map(this::toModel).toList();
    }

    @Override
    public void save(SystemMessage message) {
        if (message == null) return;
        if (message.getId() != null && findById(message.getId()).isPresent()) {
            super.updateById(message);
        } else {
            super.save(message);
        }
    }

    @Override
    public SystemMessagePO toPO(SystemMessage systemMessage) {
        if (systemMessage == null) return null;
        return SystemMessagePO.builder()
                .id(systemMessage.getId())
                .fromUserId(systemMessage.getFromUserId())
                .content(systemMessage.getContent())
                .status(systemMessage.getStatus())
                .associateUser(systemMessage.getAssociateUser())
                .type(systemMessage.getType())
                .publicTime(systemMessage.getPublicTime())
                .createTime(systemMessage.getCreateTime())
                .updateTime(systemMessage.getUpdateTime())
                .build();
    }

    @Override
    public SystemMessage toModel(SystemMessagePO po) {
        if (po == null) return null;
        return SystemMessage.builder()
                .id(po.getId())
                .fromUserId(po.getFromUserId())
                .content(po.getContent())
                .associateUser(po.getAssociateUser())
                .type(po.getType())
                .status(po.getStatus())
                .publicTime(po.getPublicTime())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}


package com.summit.stp.message.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.message.domain.model.SystemMessage;
import com.summit.stp.message.domain.repository.SystemMessageRepository;
import com.summit.stp.message.infrastructure.persistence.mapper.SystemMessageMapper;
import com.summit.stp.message.infrastructure.persistence.po.SystemMessagePO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SystemMessageRepositoryImpl implements SystemMessageRepository {
    private final SystemMessageMapper systemMessageMapper;

    public SystemMessageRepositoryImpl(SystemMessageMapper systemMessageMapper) {
        this.systemMessageMapper = systemMessageMapper;
    }
    public SystemMessagePO toPO(SystemMessage systemMessage){
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
    public SystemMessage toDomain(SystemMessagePO po){
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

    @Override
    public List<SystemMessage> list(Long userId, Integer page, Integer pageSize) {
        Page<SystemMessagePO> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<SystemMessagePO> queryWrapper = new LambdaQueryWrapper<SystemMessagePO>()
                .and(wrapper -> wrapper.eq(SystemMessagePO::getType, 1)
                        .or(w -> w.eq(SystemMessagePO::getAssociateUser, userId)))
                .orderByDesc(SystemMessagePO::getCreateTime);
        Page<SystemMessagePO> pages = systemMessageMapper.selectPage(p, queryWrapper);
        return pages.getRecords().stream().map(this::toDomain).toList();
    }

    @Override
    public void save(SystemMessage message) {
        systemMessageMapper.insert(toPO(message));
    }
}

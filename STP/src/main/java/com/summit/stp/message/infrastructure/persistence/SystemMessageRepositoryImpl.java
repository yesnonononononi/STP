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
                .status(po.getStatus())
                .publicTime(po.getPublicTime())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    @Override
    public List<SystemMessage> list(Integer page, Integer pageSize) {
        Page<SystemMessagePO> p = new Page<>(page, pageSize);
        Page<SystemMessagePO> pages = systemMessageMapper.selectPage(p, new LambdaQueryWrapper<SystemMessagePO>());
        return pages.getRecords().stream().map(this::toDomain).toList();
    }
}

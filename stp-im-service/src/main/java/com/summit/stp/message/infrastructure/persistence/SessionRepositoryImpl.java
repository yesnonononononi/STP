package com.summit.stp.message.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.message.domain.model.Session;
import com.summit.stp.message.domain.repository.SessionRepository;
import com.summit.stp.message.infrastructure.persistence.po.SessionPO;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class SessionRepositoryImpl extends AbstractRepository<Session, SessionPO> implements SessionRepository {

    public SessionRepositoryImpl(BaseMapper<SessionPO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public void save(Session session) {
        if (session == null) return;
        if (session.getId() != null && findById(session.getId()).isPresent()) {
            super.updateById(session);
        } else {
            super.save(session);
        }
    }

    @Override
    public Map<Long, Session> findByIds(List<Long> list) {
        if (list == null || list.isEmpty()) return Collections.emptyMap();
        return getBaseMapper().selectList(new LambdaQueryWrapper<SessionPO>()
                        .in(SessionPO::getId, list))
                .stream()
                .map(this::toModel)
                .collect(Collectors.toMap(Session::getId, s -> s));
    }

    @Override
    public SessionPO toPO(Session session) {
        if (session == null) return null;
        return SessionPO.builder()
                .id(session.getId())
                .type(session.getType() != null ? session.getType().getValue() : null)
                .lastMessageId(session.getLastMessageId())
                .lastMessageContent(session.getLastMessageContent())
                .lastSenderId(session.getLastSenderId())
                .lastTime(session.getLastTime())
                .createTime(session.getCreateTime())
                .updateTime(session.getUpdateTime())
                .build();
    }

    @Override
    public Session toModel(SessionPO po) {
        if (po == null) return null;
        return Session.builder()
                .id(po.getId())
                .type(po.getType() != null ? Session.Type.fromValue(po.getType()) : null)
                .lastMessageId(po.getLastMessageId())
                .lastMessageContent(po.getLastMessageContent())
                .lastSenderId(po.getLastSenderId())
                .lastTime(po.getLastTime())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}


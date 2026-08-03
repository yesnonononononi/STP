package com.summit.stp.message.infrastructure.persistence;

import com.summit.stp.message.domain.model.Session;
import com.summit.stp.message.domain.repository.SessionRepository;
import com.summit.stp.message.infrastructure.persistence.mapper.SessionMapper;
import com.summit.stp.message.infrastructure.persistence.po.SessionPO;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class SessionRepositoryImpl implements SessionRepository {
    private final SessionMapper sessionMapper;

    public SessionRepositoryImpl(SessionMapper sessionMapper) {
        this.sessionMapper = sessionMapper;
    }

    public SessionPO toPO(Session session) {
        return SessionPO.builder()
                .id(session.getId())
                .type(session.getType().getValue())
                .lastMessageId(session.getLastMessageId())
                .lastMessageContent(session.getLastMessageContent())
                .lastSenderId(session.getLastSenderId())
                .lastTime(session.getLastTime())
                .createTime(session.getCreateTime())
                .updateTime(session.getUpdateTime())
                .build();
    }

    public Session toDomain(SessionPO po) {
        return Session.builder()
                .id(po.getId())
                .type(Session.Type.fromValue(po.getType()))
                .lastMessageId(po.getLastMessageId())
                .lastMessageContent(po.getLastMessageContent())
                .lastSenderId(po.getLastSenderId())
                .lastTime(po.getLastTime())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    @Override
    public Session findById(Long sessionId) {
        SessionPO po = sessionMapper.selectById(sessionId);
        return po == null ? null : toDomain(po);
    }

    @Override
    public void save(Session session) {
        sessionMapper.insertOrUpdate(toPO(session));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Long, Session> findByIds(List<Long> list) {
        if(list.isEmpty())return Collections.EMPTY_MAP;
        return sessionMapper.selectByIds(list).stream().map(this::toDomain).collect(Collectors.toMap(Session::getId, s -> s));
    }
}

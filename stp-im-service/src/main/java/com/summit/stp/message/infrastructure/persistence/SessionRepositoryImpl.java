package com.summit.stp.message.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
                .publicId(session.getId())
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
                .id(po.getPublicId())
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
        SessionPO po = findPOByPublicId(sessionId);
        return po == null ? null : toDomain(po);
    }

    @Override
    public void save(Session session) {
        SessionPO po = toPO(session);
        SessionPO existing = findPOByPublicId(session.getId());
        if (existing == null) {
            sessionMapper.insert(po);
            return;
        }
        sessionMapper.updateById(withInternalId(po, existing.getId()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Long, Session> findByIds(List<Long> list) {
        if(list.isEmpty())return Collections.EMPTY_MAP;
        return sessionMapper.selectList(new LambdaQueryWrapper<SessionPO>()
                        .in(SessionPO::getPublicId, list))
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toMap(Session::getId, s -> s));
    }

    private SessionPO findPOByPublicId(Long publicId) {
        if (publicId == null) {
            return null;
        }
        return sessionMapper.selectOne(new LambdaQueryWrapper<SessionPO>()
                .eq(SessionPO::getPublicId, publicId));
    }

    private SessionPO withInternalId(SessionPO po, Long internalId) {
        return SessionPO.builder()
                .id(internalId)
                .publicId(po.getPublicId())
                .type(po.getType())
                .lastMessageId(po.getLastMessageId())
                .lastMessageContent(po.getLastMessageContent())
                .lastSenderId(po.getLastSenderId())
                .lastTime(po.getLastTime())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}

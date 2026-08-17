package com.summit.stp.message.application.service.impl;

import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.user.api.vo.UserSimpleVO;
import com.summit.stp.user.api.client.UserFeignClient;
import com.summit.stp.message.application.command.CreateSessionCommand;
import com.summit.stp.message.application.service.SessionService;
import com.summit.stp.message.application.vo.SessionVO;
import com.summit.stp.message.domain.model.Session;
import com.summit.stp.message.domain.model.UserSession;
import com.summit.stp.message.infrastructure.persistence.SessionRepositoryImpl;
import com.summit.stp.message.infrastructure.persistence.UserSessionRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
    private final SessionRepositoryImpl sessionRepositoryImpl;
    private final UserSessionRepositoryImpl userSessionRepositoryImpl;
    private final UserFeignClient userFeignClient;

    @Override
    public List<SessionVO> list() {
        List<UserSession> list = userSessionRepositoryImpl.findByUserId(UserHolder.getUser().getId());
        Map<Long, Session> map = sessionRepositoryImpl.findByIds(list.stream().map(UserSession::getSessionId).toList());
        return list.stream().map(userSession -> toVO(map.get(userSession.getSessionId()), userSession)).toList();
    }

    private SessionVO toVO(Session session, UserSession userSession) {
        Long targetId = userSession.getTargetId();
        String nick = userSession.getTargetNickName();
        String avatar = userSession.getTargetAvatar();

        if ((nick == null || avatar == null) && targetId != null) {
            UserSimpleVO user = userFeignClient.findSimpleUserById(targetId).getData();
            if (user != null) {
                nick = user.getNick();
                avatar = user.getAvatar();
            }
        }

        return SessionVO.builder()
                .id(session.getId())
                .targetId(targetId)
                .targetNickName(nick)
                .targetAvatar(avatar)
                .lastMessageContent(session.getLastMessageContent())
                .draft(userSession.getDraft())
                .isHidden(userSession.getIsHidden())
                .isMute(userSession.getIsMute())
                .lastTime(session.getLastTime() == null ? "" : String.valueOf(session.getLastTime().toEpochMilli()))
                .lastMessageId(session.getLastMessageId())
                .type(session.getType())
                .unreadCount(userSession.getUnreadCount())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void save(CreateSessionCommand command) {
        Long uid = UserHolder.getUser().getId();
        Long targetId = command.getTargetId();
        if (uid != null && uid.equals(targetId)) {
            throw new ParameterException("不能和自己建立会话");
        }
        Long sessionId = Session.calcSessionId(targetId, uid);
        Instant now = Instant.now();
        try {
            //1,保存公共会话
            saveSession(sessionId,now);
            //2,保存用户以及聊天对象的单向会话
            saveUserAndTargetSession(sessionId, now, command);
            return null;
        }catch (DuplicateKeyException e){
            log.info("【IM会话管理】会话已存在,uid:{},targetId:{},取消创建",uid, targetId);
        }
        return null;
    }

    @Override
    public void draft(String draft, Long sessionId) {
        UserSession userSession = userSessionRepositoryImpl.findByUserIdAndSessionId(UserHolder.getUser().getId(), sessionId);
        if(userSession == null)throw new ParameterException("会话不存在");
        userSession.setDraft(draft);
        userSessionRepositoryImpl.save(userSession);
    }

    @Override
    public void delDraft(Long sessionId) {
        UserSession userSession = userSessionRepositoryImpl.findByUserIdAndSessionId(UserHolder.getUser().getId(), sessionId);
        if(userSession == null)throw new ParameterException("会话不存在");
        userSession.setDraft(null);
        userSessionRepositoryImpl.save(userSession);
    }


    private void saveSession(Long sessionId,Instant now){
            if(now ==null){
                now = Instant.now();
            }
            Session session = Session.builder()
                    .id(sessionId)
                    .type(Session.Type.SINGLE)
                    .createTime(now)
                    .updateTime(now)
                    .build();
            sessionRepositoryImpl.save(session);
    }
    private void saveUserAndTargetSession(Long sessionId, Instant now, CreateSessionCommand command){
        Long uid = UserHolder.getUser().getId();
        Long targetId = command.getTargetId();
        userSessionRepositoryImpl.save(UserSession.builder()
                .userId(uid)
                .sessionId(sessionId)
                .createTime(now)
                .unreadCount(0)
                .targetId(targetId)
                .isHidden(0)
                .isMute(0)
                .isTop(0)
                .targetNickName(command.getTargetNickName())
                .targetAvatar(command.getTargetAvatar())
                .updateTime(now)
                .build());
        UserSimpleVO user = userFeignClient.findSimpleUserById(uid).getData();
        userSessionRepositoryImpl.save(UserSession.builder()
                    .userId(targetId)
                    .sessionId(sessionId)
                    .createTime(now)
                    .unreadCount(0)
                    .isHidden(0)
                    .isMute(0)
                    .isTop(0)
                    .updateTime(now)
                    .targetId(uid)
                    .targetAvatar(user.getAvatar())
                    .targetNickName(user.getNick())
                    .build());

    }
}


package com.summit.stp.message.application.service.impl;

import com.summit.stp.message.application.command.CreateSessionCommand;
import com.summit.stp.message.application.service.SessionService;
import com.summit.stp.message.application.vo.SessionVO;
import com.summit.stp.message.domain.exception.SessionExistException;
import com.summit.stp.message.domain.model.MessageSession;
import com.summit.stp.message.infrastructure.persistence.MessageSessionRepositoryImpl;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.user.application.UserApplicationService;
import com.summit.stp.user.application.vo.UserSimpleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
    private final MessageSessionRepositoryImpl messageSessionRepositoryImpl;
    private final UserApplicationService userApplicationService;

    @Override
    public List<SessionVO> list() {
        List<MessageSession> list = messageSessionRepositoryImpl.findSessionByUserId(UserHolder.getUser().getId());
        return list.stream().map(this::toVO).toList();
    }

    private SessionVO toVO(MessageSession session) {
        Long myId = UserHolder.getUser().getId();
        Long targetId = session.getUserId().equals(myId) ? session.getTargetId() : session.getUserId();

        String nick = session.getTargetNickName();
        String avatar = session.getTargetAvatar();

        if (!session.getUserId().equals(myId)) {
            UserSimpleVO user = userApplicationService.findSimpleUserById(targetId);
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
                .draft(session.getDraft())
                .isHidden(session.getIsHidden())
                .isMute(session.getIsMute())
                .lastTime(session.getLastTime() == null ? "" : String.valueOf(session.getLastTime().toEpochMilli()))
                .lastMessageId(session.getLastMessageId())
                .type(session.getType())
                .unreadCount(session.calcCurUnreadCount(myId))
                .build();
    }

    @Override
    public Void save(CreateSessionCommand command) {
        Long uid = UserHolder.getUser().getId();
        MessageSession messageSession = messageSessionRepositoryImpl.findById(MessageSession.calcSessionId(command.getTargetId(), uid));
        if (messageSession != null) {
            log.info("会话已存在:{}", messageSession.getId());
            return null;
        }
        Instant now = Instant.now();
        MessageSession messageSessionEntity = MessageSession.builder()
                .id(MessageSession.calcSessionId(command.getTargetId(), uid))
                .userId(uid)
                .createTime(now)
                .isHidden(0)
                .isMute(0)
                .isTop(0)
                .targetId(command.getTargetId())
                .targetNickName(command.getTargetNickName())
                .targetAvatar(command.getTargetAvatar())
                .type(MessageSession.Type.SINGLE)
                .updateTime(now)
                .build();
        messageSessionRepositoryImpl.save(messageSessionEntity);
        return null;
    }
}

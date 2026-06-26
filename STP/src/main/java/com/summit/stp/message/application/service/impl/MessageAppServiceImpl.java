package com.summit.stp.message.application.service.impl;

import com.summit.stp.message.application.service.SessionService;
import com.summit.stp.message.application.vo.*;
import com.summit.stp.shared.util.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.corundumstudio.socketio.SocketIOServer;
import com.summit.stp.common.infrastructure.websocket.Connector;
import com.summit.stp.common.infrastructure.websocket.provide.CacheProvider;
import com.summit.stp.message.application.command.CreateMessageCommand;
import com.summit.stp.message.application.service.MessageAppService;
import com.summit.stp.message.application.service.MessageSender;
import com.summit.stp.message.domain.model.MessageSession;
import com.summit.stp.message.domain.model.PrivateMessage;
import com.summit.stp.message.domain.model.SystemMessage;
import com.summit.stp.message.domain.model.SystemMessageImage;
import com.summit.stp.message.infrastructure.persistence.MessageSessionRepositoryImpl;
import com.summit.stp.message.infrastructure.persistence.PrivateMessageRepositoryImpl;
import com.summit.stp.message.infrastructure.persistence.SystemMessageImageRepositoryImpl;
import com.summit.stp.message.infrastructure.persistence.SystemMessageRepositoryImpl;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.constants.BusinessRuleConstants;
import com.summit.stp.shared.exception.ParameterException;
import com.summit.stp.shared.service.TextSafe.TextSafeServiceProvider;
import com.summit.stp.user.application.UserApplicationService;
import com.summit.stp.user.application.vo.UserSimpleVO;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageAppServiceImpl implements MessageAppService {

    private final UserApplicationService userApplicationService;
    private final PrivateMessageRepositoryImpl privateMessageRepositoryImpl;
    private final MessageSessionRepositoryImpl messageSessionRepositoryImpl;
    private final SystemMessageRepositoryImpl systemMessageRepositoryImpl;
    private final SystemMessageImageRepositoryImpl systemMessageImageRepositoryImpl;
    private final CacheProvider cacheProvider;
    private final TextSafeServiceProvider textSafeServiceProvider;


    @Override
    public List<SysMessageVO> getSysMessageList(Integer page, Integer pageSize) {
        List<SystemMessage> list = systemMessageRepositoryImpl.list(page, pageSize);
        Map<Long, List<SystemMessageImage>> map = systemMessageImageRepositoryImpl.findByIds(list.stream().map(SystemMessage::getId).toList());

        return list.stream().map(message -> this.toVO(message, map.get(message.getId()))).toList();

    }

    @Override
    public MessageVO sendMessage(CreateMessageCommand command) {
        //1,校验消息
        validateMessage(command);
        //2,校验接收者是否存在
        Long receiverId = command.getReceiverId();
        validateReceiver(receiverId);
        //3,在线状态
        boolean isOnline = StrUtil.isNotBlank(cacheProvider.getTicket(receiverId));

        // 4,落库
        Instant sendTime = DateUtil.parse(command.getSendTime());
        PrivateMessage domain = PrivateMessage.builder()
                .id(command.getMsgId() != null ? command.getMsgId() : IdUtil.getSnowflakeNextId())
                .userId(UserHolder.getUser().getId())
                .receiverId(command.getReceiverId())
                .sendTime(sendTime)
                .content(command.getContent())
                .image(command.getImage())
                .audio(command.getAudio())
                .video(command.getVideo())
                .type(PrivateMessage.Type.fromValue(command.getType()))
                .status(isOnline ? PrivateMessage.Status.READ : PrivateMessage.Status.UNREAD)
                .sessionId(MessageSession.calcSessionId(UserHolder.getUser().getId(), command.getReceiverId()))
                .build();
        privateMessageRepositoryImpl.save(domain);

        // 5,生成 VO，不透传参数，而是基于落库后的领域实体转换出来标准的时间戳
        MessageVO messageVO = toVO(domain);

        // 6,更新会话
        updateSessionMeta(messageVO, isOnline);
        return messageVO;
    }

    private void updateSessionMeta(MessageVO messageVO, boolean isOnline) {
        Long sessionId = messageVO.getSessionId();
        MessageSession session = messageSessionRepositoryImpl.findById(sessionId);
        if (session == null) {
            session = MessageSession.builder()
                    .id(sessionId)
                    .userId(UserHolder.getUser().getId())
                    .targetId(messageVO.getReceiverId())
                    .type(MessageSession.Type.SINGLE)
                    .unreadCountForUser(0)
                    .unreadCountForTarget(0)
                    .isTop(0)
                    .isHidden(0)
                    .isMute(0)
                    .createTime(Instant.now())
                    .updateTime(Instant.now())
                    .build();
        }
        String content = resolveContent(messageVO);
        session.updateLastMessage(messageVO.getId(), content, messageVO.getUserId());
        session.addUnreadCount(isOnline ? 1 : 0, UserHolder.getUser().getId());
        messageSessionRepositoryImpl.save(session);
    }

    private String resolveContent(MessageVO messageVO) {
        switch (PrivateMessage.Type.fromValue(messageVO.getType())) {
            case PrivateMessage.Type.TEXT -> {
                return messageVO.getContent();
            }
            case PrivateMessage.Type.VIDEO -> {
                return "[视频]";
            }
            case PrivateMessage.Type.AUDIO -> {
                return "[音频]";
            }
            case PrivateMessage.Type.IMAGE -> {
                return "[图片]";
            }
            case null, default -> {
                return "[未知]";
            }
        }

    }


    @Override
    public void deleteMessage(Long messageId) {
        PrivateMessage privateMessage = privateMessageRepositoryImpl.findById(messageId);
        if (!privateMessage.getUserId().equals(UserHolder.getUser().getId())) {
            throw new RuntimeException("无权操作该消息");
        }
        privateMessage.withdrawn();
        privateMessageRepositoryImpl.update(privateMessage);
    }

    @Override
    public MessageListVO getMessageHistory(Long friendId, Long cursorId, Integer limit) {
        Long newCursorId = cursorId;

        Long sessionId = MessageSession.calcSessionId(UserHolder.getUser().getId(), friendId);
        List<PrivateMessage> messages = privateMessageRepositoryImpl.findHistory(sessionId, cursorId, limit);
        List<MessageVO> list = messages.stream().map(this::toVO).toList();
        if (!list.isEmpty()) {
            newCursorId = list.getLast().getId();
        }
        boolean hasMore = list.size() >= limit;
        return MessageListVO.builder().messages(list).cursorId(newCursorId).hasMore(hasMore).sessionId(sessionId).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long readSession(Long sessionId) {
        MessageSession session = messageSessionRepositoryImpl.findById(sessionId);
        Long currentUserId = UserHolder.getUser().getId();
        List<PrivateMessage> lists = privateMessageRepositoryImpl.findMessagesBySessionId(sessionId);
        List<PrivateMessage> unreadList = lists.stream()
                .filter(m -> currentUserId.equals(m.getReceiverId()) && PrivateMessage.Status.UNREAD.equals(m.getStatus()))
                .peek(PrivateMessage::read)
                .toList();
        session.readAll(currentUserId);
        if (!unreadList.isEmpty()) {
            privateMessageRepositoryImpl.batchUpdate(unreadList);
        }
        messageSessionRepositoryImpl.save(session);
        return session.resolveTargetId(UserHolder.getUser().getId());
    }

    @Override
    public void readAll() {
        //查询用户所有会话
        Long uid = UserHolder.getUser().getId();
        List<MessageSession> sessions = messageSessionRepositoryImpl.findSessionByUserId(uid);
        //标记为已读
        List<MessageSession> list = sessions.stream().peek(session -> session.readAll(uid)).toList();
        //批量更新会话
        messageSessionRepositoryImpl.batchUpdate(list);
    }


    private SysMessageVO toVO(SystemMessage message, List<SystemMessageImage> images) {
        return SysMessageVO.builder()
                .id(message.getId())
                .images(images == null ? null : images.stream().map(SystemMessageImage::getImage).toList())
                .content(message.getContent())
                .publicTime(message.getPublicTime() == null ? "" : String.valueOf(message.getPublicTime().toEpochMilli()))
                .build();
    }

    private void validateMessage(CreateMessageCommand command) {
        //1,接收人是否存在
        Long receiverId = command.getReceiverId();
        if (receiverId == null || userApplicationService.findSimpleUserById(receiverId) == null) {
            throw new ParameterException("接收人不能为空");
        }
        //2,消息长度大小
        String content = command.getContent();
        if (StringUtil.isNullOrEmpty(content) && command.getType().equals(PrivateMessage.Type.TEXT.getValue())) {
            throw new ParameterException("消息不能为空");
        }
        if (content.length() > BusinessRuleConstants.Message.MAX_CONTENT_LENGTH) {
            throw new ParameterException("消息长度不能超过1000个字符");
        }
        //3,媒体链接是否完整
        if (!command.getType().equals(PrivateMessage.Type.TEXT.getValue()) && StrUtil.isBlank(command.getImage()) && StrUtil.isBlank(command.getAudio()) && StrUtil.isBlank(command.getVideo())) {
            throw new ParameterException("媒体链接不完整");
        }
        //4,发送时间兜底检查
        String sendTime = command.getSendTime();
        if (sendTime != null && !sendTime.isEmpty()) {
            Instant other = DateUtil.parse(sendTime);
            if (Instant.now().isBefore(other)) {
                throw new ParameterException("非法发送时间");
            }
        }
        //5,xss过滤
        content = textSafeServiceProvider.xssFilter(content);
        command.setContent(content);
    }

    private MessageVO toVO(PrivateMessage privateMessage) {
        Instant sendTime = privateMessage.getSendTime();
        PrivateMessage.Status status = privateMessage.getStatus();
        PrivateMessage.Type type = privateMessage.getType();
        return MessageVO.builder()
                .id(privateMessage.getId())
                .userId(privateMessage.getUserId())
                .receiverId(privateMessage.getReceiverId())
                .sendTime(sendTime == null ? "" : String.valueOf(sendTime.toEpochMilli()))
                .content(privateMessage.getContent())
                .status(status == null ? null : status.getValue())
                .image(privateMessage.getImage())
                .audio(privateMessage.getAudio())
                .sessionId(privateMessage.getSessionId())
                .video(privateMessage.getVideo())
                .type(type == null ? null : type.getValue())
                .build();
    }

    private void validateReceiver(Long receiverId) {
        //1,是否存在此用户
        UserSimpleVO user = userApplicationService.findSimpleUserById(receiverId);
        if (user == null) {
            throw new RuntimeException("接收者不存在");
        }
    }


}

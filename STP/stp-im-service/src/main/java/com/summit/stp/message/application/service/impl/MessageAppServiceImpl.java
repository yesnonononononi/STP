package com.summit.stp.message.application.service.impl;

import com.summit.stp.common.infrastructure.websocket.Event;
import com.summit.stp.message.application.service.MessageSender;
import com.summit.stp.message.application.vo.MessageListVO;
import com.summit.stp.common.application.vo.MessageVO;
import com.summit.stp.common.application.vo.SysMessageVO;
import com.summit.stp.message.domain.model.*;
import com.summit.stp.common.util.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.summit.stp.common.infrastructure.websocket.provide.CacheProvider;
import com.summit.stp.message.application.command.CreateMessageCommand;
import com.summit.stp.message.application.service.MessageAppService;
import com.summit.stp.message.infrastructure.persistence.PrivateMessageRepositoryImpl;
import com.summit.stp.message.infrastructure.persistence.SessionRepositoryImpl;
import com.summit.stp.message.infrastructure.persistence.SystemMessageImageRepositoryImpl;
import com.summit.stp.message.infrastructure.persistence.SystemMessageRepositoryImpl;
import com.summit.stp.message.infrastructure.persistence.UserSessionRepositoryImpl;
import com.summit.stp.common.ThreadContext.UserHolder;
import com.summit.stp.common.constants.ImConstants;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.application.service.TextSafe.TextSafeServiceProvider;
import com.summit.stp.common.feign.UserFeignClient;
import com.summit.stp.common.application.vo.UserSimpleVO;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageAppServiceImpl implements MessageAppService {

    private final UserFeignClient userFeignClient;
    private final PrivateMessageRepositoryImpl privateMessageRepositoryImpl;
    private final SessionRepositoryImpl sessionRepositoryImpl;
    private final UserSessionRepositoryImpl userSessionRepositoryImpl;
    private final SystemMessageRepositoryImpl systemMessageRepositoryImpl;
    private final SystemMessageImageRepositoryImpl systemMessageImageRepositoryImpl;
    private final CacheProvider cacheProvider;
    private final TextSafeServiceProvider textSafeServiceProvider;
    private final MessageSender messageSender;


    @Override
    public List<SysMessageVO> getSysMessageList(Integer page, Integer pageSize) {
        Long userId = UserHolder.getUser().getId();
        List<SystemMessage> list = systemMessageRepositoryImpl.list(userId, page, pageSize);
        Map<Long, List<SystemMessageImage>> map = systemMessageImageRepositoryImpl.findByIds(list.stream().map(SystemMessage::getId).toList());

        return list.stream().map(message -> this.toVO(message, map.get(message.getId()))).toList();
    }

    @Override
    public MessageVO sendMessage(CreateMessageCommand command) {
        //校验消息
        validateMessage(command);

        //校验接收者是否存在
        Long receiverId = command.getReceiverId();
        validateReceiver(receiverId);

        // 落库
        PrivateMessage domain = save(command);

        //生成 VO
        MessageVO messageVO = toVO(domain);

        //发送消息
        messageSender.send(messageVO, Event.builder().eventName(EventName.NORMAL_MESSAGE.getName()).build(),messageVO.getReceiverId(),null);

        //更新会话
        updateSessionMeta(messageVO);

        return messageVO;
    }

    private void updateSessionMeta(MessageVO messageVO ) {
        Long sessionId = messageVO.getSessionId();
        Long senderId = UserHolder.getUser().getId();
        Long receiverId = messageVO.getReceiverId();
        Session session = sessionRepositoryImpl.findById(sessionId);
        if (session == null) {
            session = Session.builder()
                    .id(sessionId)
                    .type(Session.Type.SINGLE)
                    .createTime(Instant.now())
                    .updateTime(Instant.now())
                    .build();
        }
        String content = resolveContent(messageVO);
        session.updateLastMessage(messageVO.getId(), content, messageVO.getUserId());
        sessionRepositoryImpl.save(session);
        ensureUserSession(senderId, sessionId, 0, null, null);
        UserSession receiverSession = ensureUserSession(receiverId, sessionId, 0, null, null);
        receiverSession.addUnreadCount(1);
        userSessionRepositoryImpl.save(receiverSession);
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


    private PrivateMessage save(CreateMessageCommand command){
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
                .status( PrivateMessage.Status.UNREAD)
                .sessionId(Session.calcSessionId(UserHolder.getUser().getId(), command.getReceiverId()))
                .build();
        privateMessageRepositoryImpl.save(domain);
        return domain;
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
        try {
            Long newCursorId = cursorId;

            Long sessionId = Session.calcSessionId(UserHolder.getUser().getId(), friendId);
            List<PrivateMessage> messages = privateMessageRepositoryImpl.findHistory(sessionId, cursorId, limit);
            List<MessageVO> list = messages.stream().map(this::toVO).toList();
            if (!list.isEmpty()) {
                newCursorId = list.getLast().getId();
            }
            boolean hasMore = list.size() >= limit;
            return MessageListVO.builder().messages(list).cursorId(newCursorId).hasMore(hasMore).sessionId(sessionId).build();
        }catch (Exception e){
            log.error("【获取消息历史】异常: ", e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long readSession(Long sessionId) {
        Long currentUserId = UserHolder.getUser().getId();
        UserSession userSession = userSessionRepositoryImpl.findByUserIdAndSessionId(currentUserId, sessionId);
        if (userSession == null) {
            throw new ParameterException("会话不存在");
        }
        UserSession targetSession = userSessionRepositoryImpl.findTargetSession(sessionId, currentUserId);
        List<PrivateMessage> lists = privateMessageRepositoryImpl.findMessagesBySessionId(sessionId);
        List<PrivateMessage> unreadList = lists.stream()
                .filter(m -> currentUserId.equals(m.getReceiverId()) && PrivateMessage.Status.UNREAD.equals(m.getStatus()))
                .peek(PrivateMessage::read)
                .toList();
        userSession.readAll();
        if (!unreadList.isEmpty()) {
            privateMessageRepositoryImpl.batchUpdate(unreadList);
        }
        userSessionRepositoryImpl.save(userSession);
        return targetSession == null ? null : targetSession.getUserId();
    }

    @Override
    public void readAll() {
        //查询用户所有会话
        Long uid = UserHolder.getUser().getId();
        List<UserSession> sessions = userSessionRepositoryImpl.findByUserId(uid);
        //标记为已读
        List<UserSession> list = sessions.stream().peek(UserSession::readAll).toList();
        //批量更新会话
        userSessionRepositoryImpl.batchUpdate(list);
    }


    /**
     * 查询用户会话,如果不存在自动初始化并保存
     * @param userId 用户ID
     * @param sessionId 会话id
     * @param unreadCount 未读消息数量
     * @param targetNickName 目标用户昵称
     * @param targetAvatar 目标用户头像
     * @return 用户会话
     */
    private UserSession ensureUserSession(Long userId, Long sessionId, Integer unreadCount, String targetNickName, String targetAvatar) {
        UserSession userSession = userSessionRepositoryImpl.findByUserIdAndSessionId(userId, sessionId);
        if (userSession != null) {
            return userSession;
        }
        Instant now = Instant.now();
        userSession = UserSession.builder()
                .userId(userId)
                .sessionId(sessionId)
                .isTop(0)
                .isMute(0)
                .unreadCount(unreadCount)
                .isHidden(0)
                .targetNickName(targetNickName)
                .targetAvatar(targetAvatar)
                .createTime(now)
                .updateTime(now)
                .build();
        userSessionRepositoryImpl.save(userSession);
        return userSession;
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
        if (receiverId == null || userFeignClient.findSimpleUserById(receiverId).getData() == null) {
            throw new ParameterException("接收人不能为空");
        }
        //2,消息长度大小
        String content = command.getContent();
        if (StringUtil.isNullOrEmpty(content) && command.getType().equals(PrivateMessage.Type.TEXT.getValue())) {
            throw new ParameterException("消息不能为空");
        }
        if (content.length() > ImConstants.Business.MAX_CONTENT_LENGTH) {
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
        UserSimpleVO user = userFeignClient.findSimpleUserById(receiverId).getData();
        if (user == null) {
            throw new RuntimeException("接收者不存在");
        }
    }


}

package com.summit.stp.message.application.vo;

import com.summit.stp.message.domain.model.Session;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class SessionVO {
    private final Long id;
    private final Long userId;
    private final Long targetId;
    private Session.Type type;
    private  Long lastMessageId;
    private String lastMessageContent;
    private Long lastSenderId;
    private String lastTime;
    private Integer unreadCount;
    private Integer isTop;
    private Integer isMute;
    private String draft;
    private Integer isHidden;
    private final String targetNickName;
    private final String targetAvatar;

}

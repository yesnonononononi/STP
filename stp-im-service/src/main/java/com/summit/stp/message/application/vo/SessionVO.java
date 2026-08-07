package com.summit.stp.message.application.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.summit.stp.message.domain.model.Session;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SessionVO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Long userId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Long targetId;
    private Session.Type type;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private  Long lastMessageId;
    private String lastMessageContent;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
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

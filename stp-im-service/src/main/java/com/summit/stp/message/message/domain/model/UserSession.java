package com.summit.stp.message.message.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class UserSession {
    private final Long id;
    private final Long userId;
    private final Long sessionId;
    private Integer isTop;
    private Integer isMute;
    private Long targetId;
    private String draft;
    private Integer unreadCount;
    private Integer isHidden;
    private String targetNickName;
    private String targetAvatar;
    private final Instant createTime;
    private Instant updateTime;

    public void readAll() {
        this.unreadCount = 0;
        this.updateTime = Instant.now();
    }

    public void addUnreadCount(int offset) {
        this.unreadCount = (this.unreadCount == null ? 0 : this.unreadCount) + offset;
        this.updateTime = Instant.now();
    }

    public void setDraft(String draft) {
        this.draft = draft;
        this.updateTime = Instant.now();
    }
}

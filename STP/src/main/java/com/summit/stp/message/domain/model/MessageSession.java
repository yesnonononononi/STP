package com.summit.stp.message.domain.model;


import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

import static java.util.Objects.hash;

@Builder
@Getter
public class MessageSession {
    private final Long id;
    private final Long userId;
    private final Long targetId;
    private Type type;
    private  Long lastMessageId;
    private String lastMessageContent;
    private Long lastSenderId;
    private Instant lastTime;
    private Integer unreadCountForUser;
    private Integer unreadCountForTarget;
    private Integer isTop;
    private Integer isMute;
    private String draft;
    private Integer isHidden;
    private final String targetNickName;
    private final String targetAvatar;
    private final Instant createTime;
    private Instant updateTime;

    public void readAll(Long id) {
        if(id.equals(this.userId)){
            this.unreadCountForUser = 0;
        }
        else if(id.equals(this.targetId)){
            this.unreadCountForTarget = 0;
        }
        this.updateTime = Instant.now();
    }

    /**
     * 根据id获取会话对象中的另一个用户id
     * @param id 当前用户id
     * @return 对方用户id
     */
    public Long resolveTargetId(Long id) {
        return id.equals(this.userId) ? this.targetId : this.userId;
    }

    /**
     * 根据当前用户ID计算未读消息数
     * @param curId 当前用户id
     * @return 未读消息数
     */
    public Integer calcCurUnreadCount(Long curId) {
        return curId.equals(this.userId) ? this.unreadCountForUser : this.unreadCountForTarget;
    }

    @Getter
    public enum Type{
        SINGLE(1),
        GROUP(2);
        private final  int value;
        Type(int value){
            this.value = value;
        }
        public static Type fromValue(int value){
            for(Type type : values()){
                if(type.getValue() == value){
                    return type;
                }
            }
            return null;
        }
    }

    public void updateLastMessage(Long lastId,String lastContent,Long lastSenderId){
        this.lastMessageId = lastId;
        this.lastMessageContent = lastContent;
        this.lastSenderId = lastSenderId;
        Instant now = Instant.now();
        this.lastTime = now;
        this.updateTime = now;
    }

    public void addUnreadCount(int offset,Long uid){
        if(uid.equals(this.userId)){
            this.unreadCountForTarget+=offset;
        }
        if(uid.equals(this.targetId)){
            this.unreadCountForUser+=offset;
        }
        this.updateTime = Instant.now();
    }

    public static Long calcSessionId(Long friendId,Long userId){
        return (long) hash(Math.max(userId,friendId),Math.min(userId,friendId));
    }
}

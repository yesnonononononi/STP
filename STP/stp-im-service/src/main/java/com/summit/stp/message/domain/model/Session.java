package com.summit.stp.message.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

import static java.util.Objects.hash;

@Builder
@Getter
public class Session {
    private final Long id;
    private Type type;
    private Long lastMessageId;
    private String lastMessageContent;
    private Long lastSenderId;
    private Instant lastTime;
    private final Instant createTime;
    private Instant updateTime;

    @Getter
    public enum Type{
        SINGLE(1),
        GROUP(2);
        private final int value;
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

    public void updateLastMessage(Long lastId, String lastContent, Long lastSenderId){
        this.lastMessageId = lastId;
        this.lastMessageContent = lastContent;
        this.lastSenderId = lastSenderId;
        Instant now = Instant.now();
        this.lastTime = now;
        this.updateTime = now;
    }

    public static Long calcSessionId(Long friendId, Long userId){
        return (long) hash(Math.max(userId, friendId), Math.min(userId, friendId));
    }
}

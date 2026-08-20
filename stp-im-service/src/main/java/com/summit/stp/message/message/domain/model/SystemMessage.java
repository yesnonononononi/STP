package com.summit.stp.message.message.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
public class SystemMessage {
    private final Long id;
    private final Long fromUserId;
    private final List<String> images;
    private String content;
    private SystemMessageStatus status;
    private final Long associateUser;
    private final SystemMessageType type; // 1广播 2单独用户
    private Instant publicTime;
    private final Instant createTime;
    private Instant updateTime;


    @Getter
    public enum SystemMessageType{
        BROADCAST(1),
        PRIVATE(2);

        private final int code;
        SystemMessageType(int code) {
            this.code = code;
        }
        public static SystemMessageType fromCode(int code){
            for (SystemMessageType value : values()) {
                if (value.code == code) {
                    return value;
                }
            }
           return null;
        }
    }

    @Getter
    public enum SystemMessageStatus{
        DRAFT(0),
        PUBLISHED(1),
        DELETED(3),
        REVOKED(2);

        private final int code;
        SystemMessageStatus(int code) {
            this.code = code;
        }
        public static SystemMessageStatus fromCode(int code){
            for (SystemMessageStatus value : values()) {
                if (value.code == code) {
                    return value;
                }
            }
           return null;
        }
    }

    public void draft(){
        this.status = SystemMessageStatus.DRAFT;
        this.updateTime = Instant.now();
    }

    public void publish(){
        this.status = SystemMessageStatus.PUBLISHED;
        this.updateTime = Instant.now();
        this.publicTime = Instant.now();
    }

    public void revoke(){
        this.status = SystemMessageStatus.REVOKED;
        this.updateTime = Instant.now();
    }
    public void delete(){
        this.status = SystemMessageStatus.DELETED;
        this.updateTime = Instant.now();
    }
}

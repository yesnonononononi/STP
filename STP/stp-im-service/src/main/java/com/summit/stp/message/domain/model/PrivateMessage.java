package com.summit.stp.message.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
@Builder
@Getter
public class PrivateMessage {

    private final Long id;

    private final Long userId;

    private final Long receiverId;

    private final Instant sendTime;

    private String content;

    private String image;

    private String audio;

    private String video;

    private Type type;

    private Status status;

    private Long sessionId;

    public void withdrawn() {
        this.status = Status.WITHDRAWN;
    }

    public void read() {
        this.status = Status.READ;
    }

    @Getter
    public enum Type{
        IMAGE(1),
        TEXT(2),
        AUDIO(3),
        VIDEO(4);
        private final  int value;
        Type(int value){
            this.value = value;
        }
        public static Type fromValue(int value){
            for(Type type : Type.values()){
                if(type.getValue() == value){
                    return type;
                }
            }
            return null;
        }
    }
    @Getter
    public enum Status{
        UNREAD(1),
        READ(2),
        WITHDRAWN(3);
        private final  int value;
        Status(int value){
            this.value = value;
        }
        public static Status fromValue(int value){
            for(Status status : Status.values()){
                if(status.getValue() == value){
                    return status;
                }
            }
            return null;
        }
    }
}

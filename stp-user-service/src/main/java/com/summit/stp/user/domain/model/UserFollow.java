package com.summit.stp.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserFollow {
    private Long id;
    private Long followerId;
    private Long followeeId;
    private Timestamp createTime;
    private FollowStatus status;
    private String source;

    public void cancel() {
        this.status = FollowStatus.CANCEL;
    }
    public boolean isActive() {
        return this.status != FollowStatus.CANCEL;
    }

    public void follow() {
        this.status = FollowStatus.NORMAL;
    }

    public void eachFollow() {
        this.status = FollowStatus.EACH;
    }

    public boolean isEachFollow() {
        return this.status == FollowStatus.EACH;
    }

    @Getter
    public static enum FollowStatus{
            NORMAL(1),
            CANCEL(2),
            EACH(3);
            private final int code;
            FollowStatus(int code) {
                this.code = code;
            }
            public static FollowStatus fromCode(int code){
                for (FollowStatus value : values()) {
                    if(value.getCode() == code)
                        return value;
                }
                throw new IllegalArgumentException("Invalid FollowStatus code");
            }


    }
}

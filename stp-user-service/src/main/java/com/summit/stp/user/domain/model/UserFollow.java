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
}

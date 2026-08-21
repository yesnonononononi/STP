package com.summit.stp.user.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Builder
@Getter
public class UserStat {
    private Long userId;
    private Long fans;
    private Long topic;
    private Long liked;
}

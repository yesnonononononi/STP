package com.summit.stp.user.domain.model.stats;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserTrendStat {
    private List<String> dates;
    private List<Integer> newUserCountList;
}

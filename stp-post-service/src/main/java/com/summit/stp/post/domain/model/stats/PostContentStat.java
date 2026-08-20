package com.summit.stp.post.domain.model.stats;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PostContentStat {
    private List<String> dates;
    private List<Integer> postCountList;
    private List<Integer> commentCountList;
    private List<Integer> blockedCountList;
}

package com.summit.stp.post.api.vo.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostContentStatsVO implements Serializable {
    private List<String> dates;
    private List<Integer> postCountList;
    private List<Integer> commentCountList;
    private List<Integer> blockedCountList;
}

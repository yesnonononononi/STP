package com.summit.stp.post.infrastructure.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostTrendStatDTO {
    private String dateStr;
    private Integer postCnt;
    private Integer commentCnt;
    private Integer blockedCnt;
}

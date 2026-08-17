package com.summit.stp.admin.api.dto;

import com.summit.stp.common.application.api.dto.RangeDTO;
import lombok.Data;

import java.security.Timestamp;

@Data
public class AdminPostQueryRequest {
    private Integer page;
    private RangeDTO<Timestamp> createTime;
    private RangeDTO<Timestamp> updateTime;
    private Integer pageSize;
    private String keyword;
    private Integer status;
    private Long creatorId;
    private RangeDTO<Long> likeCount;
    private RangeDTO<Long> comment;
}

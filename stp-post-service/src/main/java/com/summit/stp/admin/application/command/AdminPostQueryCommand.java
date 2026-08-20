package com.summit.stp.admin.application.command;

import com.summit.stp.common.application.api.dto.RangeDTO;
import lombok.Builder;
import lombok.Data;

import java.security.Timestamp;

@Builder
@Data
public class AdminPostQueryCommand {
  private Long postId;
  private Integer page;
  private Integer pageSize;
  private String keyword;
  private Integer status;
  private Long creatorId;
  private RangeDTO<Long> likeCount;
  private RangeDTO<Long> comment;
  private RangeDTO<Timestamp> createTime;
  private RangeDTO<Timestamp> updateTime;
}

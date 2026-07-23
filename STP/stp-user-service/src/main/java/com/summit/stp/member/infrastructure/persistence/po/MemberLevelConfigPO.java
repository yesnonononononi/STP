package com.summit.stp.member.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("member_level_config")
public class MemberLevelConfigPO {
  @TableId(type = IdType.INPUT)
  private Long level;
  private String levelName;
  private double minRecharge;
  private String privilegesJson;
  private String iconUrl;
  private long sortOrder;
}

package com.summit.stp.user.infrastructure.persistence.po;

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
@TableName("user_stat")
public class UserStatPO {
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;
    private Long fans;
    private Long topic;
    private Long liked;
}

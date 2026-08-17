package com.summit.stp.rank_board.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("post_rank")
public class PostRankPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private BigDecimal score;
    @TableField("`rank`")
    private Integer rank;
    private LocalDate periodDate;
    private Timestamp createTime;
    private Timestamp updateTime;
}


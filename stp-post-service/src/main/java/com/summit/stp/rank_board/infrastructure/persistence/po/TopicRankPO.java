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
@TableName("topic_rank")
public class TopicRankPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(fill = FieldFill.INSERT)
    private Long publicId;
    private Long tagId;
    private BigDecimal score;

    @TableField("`rank`")
    private Integer rank;
    private LocalDate periodDate;
    private Timestamp createTime;
    private Timestamp updateTime;
}

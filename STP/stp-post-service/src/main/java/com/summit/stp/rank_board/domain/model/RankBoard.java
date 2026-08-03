package com.summit.stp.rank_board.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Builder
@Getter
public class RankBoard {
    private final Long id;
    /**
     * 排行榜名称
     */
    private  final  String name;

    /**
     * 周开始时间
     */
    private final Timestamp weekStartDate;
    /**
     * 实体id
     */
    private final Long entityId;
    /**
     * 实体名称
     */
    private String entityName;
    /**
     * 分数
     */
    private Double score;
    /**
     * 排名
     */
    private Integer rank;
    /**
     * 状态 1:正常 0:禁用
     */
    private Integer status;
    private Timestamp updateTime;
    /**
     * 类型
     */
    private final String type;
    /**
     * 背景图片
     */
    private String bgImg;


    public void updateRank(int newRank) {
        this.rank = newRank;
    }
}

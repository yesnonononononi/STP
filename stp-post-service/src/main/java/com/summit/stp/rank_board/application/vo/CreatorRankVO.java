package com.summit.stp.rank_board.application.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.summit.stp.user.api.vo.UserSimpleVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreatorRankVO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String name;
    private Timestamp week_start_date;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long entityId;
    private String entityName;
    private UserSimpleVO entityInfo;
    private Double score;
    private Integer rank;
    private String type;
    private String bgImg;
}


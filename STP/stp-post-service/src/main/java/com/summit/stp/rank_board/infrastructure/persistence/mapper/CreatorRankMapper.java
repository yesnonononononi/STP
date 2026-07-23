package com.summit.stp.rank_board.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.rank_board.infrastructure.persistence.po.CreatorRankPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CreatorRankMapper extends BaseMapper<CreatorRankPO> {
}

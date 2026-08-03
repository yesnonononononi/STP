package com.summit.stp.rank_board.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.rank_board.infrastructure.persistence.po.TopicRankPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TopicRankMapper extends BaseMapper<TopicRankPO> {
}

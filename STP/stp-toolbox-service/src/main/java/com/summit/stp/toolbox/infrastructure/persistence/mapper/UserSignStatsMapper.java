package com.summit.stp.toolbox.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.toolbox.infrastructure.persistence.po.UserSignStatsPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSignStatsMapper extends BaseMapper<UserSignStatsPO> {
}

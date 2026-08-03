package com.summit.stp.toolbox.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.toolbox.infrastructure.persistence.po.UserSignLogPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSignLogMapper extends BaseMapper<UserSignLogPO> {
}

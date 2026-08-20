package com.summit.stp.admin.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.admin.infrastructure.persistence.po.SystemActivityPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemActivityMapper extends BaseMapper<SystemActivityPO> {
}

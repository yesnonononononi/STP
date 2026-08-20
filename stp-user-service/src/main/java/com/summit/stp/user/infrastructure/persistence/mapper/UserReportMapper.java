package com.summit.stp.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.user.infrastructure.persistence.po.UserReportPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserReportMapper extends BaseMapper<UserReportPO> {
}

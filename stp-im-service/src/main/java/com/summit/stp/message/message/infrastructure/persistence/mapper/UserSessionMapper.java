package com.summit.stp.message.message.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.message.message.infrastructure.persistence.po.UserSessionPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSessionMapper extends BaseMapper<UserSessionPO> {
}

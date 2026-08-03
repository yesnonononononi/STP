package com.summit.stp.message.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.message.infrastructure.persistence.po.PrivateMessagePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessagePO> {
}

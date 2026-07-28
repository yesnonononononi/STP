package com.summit.stp.message.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.message.infrastructure.persistence.po.InteractionMessagePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InteractionMessageMapper extends BaseMapper<InteractionMessagePO> {
}

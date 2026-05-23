package com.summit.stp.member.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.member.infrastructure.persistence.po.MemberLevelConfigPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberLevelConfigMapper extends BaseMapper<MemberLevelConfigPO> {
}

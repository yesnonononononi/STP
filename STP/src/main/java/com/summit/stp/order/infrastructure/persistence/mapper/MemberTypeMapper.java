package com.summit.stp.order.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.member.infrastructure.persistence.po.MemberTypePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberTypeMapper extends BaseMapper<MemberTypePO> {
}

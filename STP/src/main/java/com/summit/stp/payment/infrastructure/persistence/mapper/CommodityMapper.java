package com.summit.stp.payment.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.payment.infrastructure.persistence.po.CommodityPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommodityMapper extends BaseMapper<CommodityPO>
{
}

package com.summit.stp.coupon.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.coupon.infrastructure.persistence.po.UserCouponPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCouponPO> {
}

package com.summit.stp.coupon.infrastructure.persistence.mapper;

import cn.hutool.core.lang.Opt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.coupon.infrastructure.persistence.po.CouponActivityPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CouponActivityMapper extends BaseMapper<CouponActivityPO> {
    void batchUpdate(Map<Object, Object> stockOfActivities);


    @Select("select discount, amount,  scope_type, time_type, valid_days, valid_hours, image, description," +
            "ca.id, coupon_id, ca.name, stock,ca.type, activity_start_time, activity_end_time, ca.status, ca.create_time, ca.update_time, limit_quantity" +
            " from coupon_activity ca left join coupon c  on ca.coupon_id = c.id where c.status = 1 and c.scope_type = #{scopeType} ")
    List<CouponActivityPO> selectByScopeType(Integer scopeType);
}

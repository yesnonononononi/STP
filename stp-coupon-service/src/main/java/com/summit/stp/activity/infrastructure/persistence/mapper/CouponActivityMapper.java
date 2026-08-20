package com.summit.stp.activity.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.activity.infrastructure.persistence.po.CouponActivityPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface CouponActivityMapper extends BaseMapper<CouponActivityPO> {
    void batchUpdate(Map<Object, Object> stockOfActivities);

    @Select("select discount, amount, scope_type, time_type, valid_days, valid_hours, image, description," +
            "ca.id, coupon_id, ca.name, stock, ca.type, activity_start_time, activity_end_time, ca.status, ca.create_time, ca.update_time" +
            " from coupon_activity ca left join coupon c on ca.coupon_id = c.id where c.status = 1 and c.scope_type = #{scopeType} ")
    List<CouponActivityPO> selectByScopeType(Integer scopeType);

    @Update("update coupon_activity ca set ca.stock = ca.stock-1 where ca.coupon_id =#{couponId} and stock > 0")
    int deductStockWithOptimisticLock( Long couponId);

}

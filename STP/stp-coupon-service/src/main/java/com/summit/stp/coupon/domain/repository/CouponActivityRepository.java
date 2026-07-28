package com.summit.stp.coupon.domain.repository;


import com.summit.stp.coupon.domain.model.CouponActivity;

import java.util.List;
import java.util.Map;

public interface CouponActivityRepository {
    CouponActivity findById(Long id);
    List<CouponActivity> findAll();
    void save(CouponActivity activity);
    void update(CouponActivity activity);
    void delete(Long id);

    void updateStockByActivityId(Object activityId, Object stock);

    void batchUpdate(Map<Object, Object> stockOfActivities);

    List<CouponActivity> findByScopeType(Integer scopeType);
}

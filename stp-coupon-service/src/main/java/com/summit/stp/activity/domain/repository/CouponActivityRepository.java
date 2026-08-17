package com.summit.stp.activity.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.activity.domain.model.CouponActivity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CouponActivityRepository {
    Optional<CouponActivity> findById(Long id);
    void save(CouponActivity activity);
    void update(CouponActivity activity);
    void delete(Long id);
    void updateStockByActivityId(Object activityId, Object stock);
    void batchUpdate(Map<Object, Object> stockOfActivities);
    List<CouponActivity> findByScopeType(Integer scopeType);

    Page<CouponActivity> list(String keyword, Integer status, Integer page, Integer pageSize);
}

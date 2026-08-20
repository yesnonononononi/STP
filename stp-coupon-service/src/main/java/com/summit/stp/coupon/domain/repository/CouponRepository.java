package com.summit.stp.coupon.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.coupon.domain.model.Coupon;

import java.util.List;
import java.util.Optional;

/**
 * 优惠券模板仓储接口
 */
public interface CouponRepository {
    Optional<Coupon> findById(Long id);
    void update(Coupon template);

    List<Coupon> findByIds(List<Long> cList);

    Page<Coupon> list(String keyword, Integer status, Integer page, Integer pageSize);

    void save(Coupon coupon);

    void updateById(Coupon coupon);

    void deleteById(Long id);
}

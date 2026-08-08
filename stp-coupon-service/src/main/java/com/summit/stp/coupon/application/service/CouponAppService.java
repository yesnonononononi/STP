package com.summit.stp.coupon.application.service;

import com.summit.stp.common.application.api.vo.CouponQueryVO;

import java.util.List;
import java.util.Map;

public interface CouponAppService {
    /**
     * 根据ID查询优惠券模板详情
     */
    CouponQueryVO queryById(Long id);

    /**
     * 批量查询优惠券模板
     */
    Map<Long, CouponQueryVO> queryByIds(List<Long> ids);
}

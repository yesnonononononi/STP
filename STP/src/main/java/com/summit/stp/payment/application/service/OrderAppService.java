package com.summit.stp.payment.application.service;

import com.summit.stp.payment.application.vo.OrderQueryVO;

import java.util.List;

/**
 * 订单应用服务接口
 */
public interface OrderAppService {
    /**
     * 根据订单ID查询单个订单详情
     */
    OrderQueryVO queryById(Long orderId);

    /**
     * 删除订单
     */
    void deleteById(Long orderId);

    /**
     * 分页查询历史订单
     */
    List<OrderQueryVO> queryHistory(long page, long pageSize);

    /**
     * 确认订单
     * @param orderId 订单ID
     */
    void ackOrder(Long orderId);
}

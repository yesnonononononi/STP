package com.summit.stp.order.application.service;

import com.summit.stp.order.api.dto.OrderCreateRequest;
import com.summit.stp.order.application.vo.OrderQueryVO;
import com.summit.stp.payment.application.vo.PayVO;
import com.summit.stp.payment.domain.model.PayType;
import com.summit.stp.shared.result.Result;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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


    Result<PayVO> createOrder(OrderCreateRequest orderCreateRequest);


    /**
     * 取消单笔超时订单
     * @param orderId
     */
    void cancelOrderTimeout(Long orderId);

    /**
     * 标记订单超时
     * @param orderId
     */
    void timeoutOrder(Set<String> orderId);

    /**
     * 取消订单（退还优惠券等业务处理）
     */
    void cancelOrder(Long orderId);
}

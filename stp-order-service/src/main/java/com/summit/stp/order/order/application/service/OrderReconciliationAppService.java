package com.summit.stp.order.order.application.service;

import com.summit.stp.common.application.api.result.Result;

public interface OrderReconciliationAppService {
    /**
     * 对账单个订单并更新/补偿本地权益与状态
     * @param orderId 本地订单ID
     * @return 对账处理结果说明
     */

    Result<String> reconcileOrder(Long orderId, boolean requireCompensation);

    /**
     * 对订单进行权益补偿,此举并未校验订单状态
     * @param orderId 订单号
     */
    Result<Void> compensateOrder(Long orderId);
}

package com.summit.stp.order.application.service;

import com.summit.stp.common.application.api.result.Result;

public interface OrderReconciliationAppService {
    /**
     * 对账单个订单并更新/补偿本地权益与状态
     * @param orderId 本地订单ID
     * @return 对账处理结果说明
     */
    Result<String> reconcileOrder(Long orderId);


}

package com.summit.stp.order.application.service;

public interface OrderReconciliationAppService {
    /**
     * 对账单个订单并更新/补偿本地权益与状态
     * @param orderId 本地订单ID
     * @return 对账处理结果说明
     */
    String reconcileOrder(Long orderId);

    /**
     * 批量扫描并对账最近24小时内处于未完成状态的订单
     */
    void reconcilePendingOrders();
}

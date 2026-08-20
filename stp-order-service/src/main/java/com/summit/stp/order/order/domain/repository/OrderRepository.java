package com.summit.stp.order.order.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.RepositoryTemplate;
import com.summit.stp.order.admin.application.command.AdminQueryCommand;
import com.summit.stp.order.api.vo.OrderQueryVO;
import com.summit.stp.order.order.domain.model.Order;
import com.summit.stp.order.order.infrastructure.persistence.po.OrderPO;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 订单仓储接口
 */
public interface OrderRepository extends RepositoryTemplate<Order, OrderPO> {
    Long generateOrderId();

    void save(Order order);

    void updateById(Order order);

    List<Order> queryHistoryOrders(long page, long pageSize, Integer status);

    Order findOrderById(Long orderId);

    void deleteById(Long orderId);


    Map<Long, OrderQueryVO> findOrderByCouponIds(Long currentUserId, List<Long> ids);

    List<Order> findOrderByIds(Collection<Long> orderIds);

    /**
     * 批量查询待支付且已超过订单超时时间的订单。
     *
     * @param currentTime 当前时间
     * @param limit       本次最多查询数量
     * @return 已超时的待支付订单
     */
    List<Order> findPendingExpiredOrders(Timestamp currentTime, int limit);

    void batchUpdate(List<Order> changedOrders);

    Page<Order> findPage(AdminQueryCommand command);


    void ackOrderIfWaitPay(Long orderNo);

    Optional<Order> findById(Long orderNo);

    com.summit.stp.order.order.domain.model.stats.OrderOverviewStat countOrderOverview();

    com.summit.stp.order.order.domain.model.stats.OrderTrendStat countOrderTrends(int days);
}


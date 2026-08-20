package com.summit.stp.order.admin.application.service;

import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.order.admin.application.command.AdminQueryCommand;
import com.summit.stp.order.admin.application.vo.AdminOrderVO;

import java.util.List;

public interface AdminOrderService {
    /**
     * 分页筛选查询订单列表
     * @param command 查询命令
     */
    Result<PageResult<List<AdminOrderVO>>> listBy(AdminQueryCommand command);

    /**
     * 补单
     * @param orderNo 订单号
     */
    Result<Void> reissuance(Long orderNo);

    /**
     * 退款
     * @param orderNo 订单号
     * @param reason 退款原因
     */
    Result<Void> refund(Long orderNo, String reason);

    /**
     * 核对三方订单状态
     *
     * @param orderNo 订单号
     */
    Result<String> verifyStatus(Long orderNo);
}

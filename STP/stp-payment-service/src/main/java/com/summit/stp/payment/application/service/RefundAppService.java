package com.summit.stp.payment.application.service;

import com.summit.stp.payment.application.command.RefundCommand;
import com.summit.stp.payment.application.vo.RefundResultVO;

public interface RefundAppService {
    /**
     * 发起退款请求
     * @param command 退款指令
     * @return 退款处理结果
     */
    RefundResultVO refund(RefundCommand command);
}

package com.summit.stp.payment.application.service;


import com.summit.stp.payment.application.command.PayCallbackCheckCommand;
import com.summit.stp.payment.application.command.PayCommand;
import com.summit.stp.payment.application.vo.PayVO;
import com.summit.stp.shared.result.Result;

import java.util.List;

public interface PayAppService {
    /**
     * 订单支付
     * @param payCommand 支付命令
     * @return 支付信息参数
     */
    PayVO pay(PayCommand payCommand);

    /**
     * 支付结果通知回调
     * @param payResult 支付结果
     */
    void checkResult(PayCallbackCheckCommand payResult);

    /**
     * 获取支付方式列表
     * @return
     */
    Result<List<String>> getType();
}

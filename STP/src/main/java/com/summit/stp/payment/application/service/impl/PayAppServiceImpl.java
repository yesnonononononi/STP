package com.summit.stp.payment.application.service.impl;

import com.summit.stp.payment.application.command.PayCallbackCheckCommand;
import com.summit.stp.payment.application.command.PayCommand;
import com.summit.stp.payment.application.service.PayAppService;
import com.summit.stp.payment.application.vo.PayVO;
import com.summit.stp.payment.domain.PayDomainService;
import com.summit.stp.payment.domain.model.*;
import com.summit.stp.payment.domain.event.PaySuccessEvent;
import com.summit.stp.payment.domain.event.PayFailEvent;

import java.util.Arrays;
import java.util.HashMap;
import com.summit.stp.payment.application.service.PayEventPublishProvider;
import com.summit.stp.shared.exception.BusinessException;
import com.summit.stp.shared.result.Result;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class PayAppServiceImpl implements PayAppService {
    private final PayDomainService payService;
    private final PayEventPublishProvider payEventPublishProvider;

    @Value("${payment.pay.success.symbol}")
    private String successSymbol;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PayVO pay(PayCommand payCommand) {

        Long orderId = payCommand.getOrderId();
        Timestamp timestamp = payCommand.getTimestamp();
        String memberName = payCommand.getMemberName();
        BigDecimal amount = payCommand.getAmount();
        PayType payType = payCommand.getPayType();
        // 6. 生成签名
        String sign = payService.generateSign(payType, orderId, memberName, amount, timestamp);

        // 8. 返回支付参数
        return payService.buildResponse(payType, orderId, amount, memberName, sign, timestamp);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkResult(PayCallbackCheckCommand payResult) {
        //验证签名（使用回调所有参数构建待签名字符串）
        if (!checkSign(payResult)) {
            throw new BusinessException("签名验证失败");
        }
        //是否支付成功
        if(payResult.getTrade_status().equals(successSymbol)) {
            //发布支付成功事件并带上商户订单ID
            payEventPublishProvider.publish(new PaySuccessEvent(payResult.getOut_trade_no()));
        }else{
            payEventPublishProvider.publish(new PayFailEvent(payResult.getOut_trade_no(), payResult.getTrade_status()));
        }
    }

    @Override
    public Result<List<String>> getType() {
        return Result.success(Arrays.stream(PayType.values()).map(PayType::getType).toList());
    }


    /**
     * 商户公钥验证签名
     *
     * @param payResult
     * @return
     */
    private boolean checkSign(PayCallbackCheckCommand payResult) {
        Map<String, String> params = new HashMap<>();
        if (payResult.getPid() != null) params.put("pid", payResult.getPid());
        if (payResult.getTrade_no() != null) params.put("trade_no", String.valueOf(payResult.getTrade_no()));
        if (payResult.getOut_trade_no() != null)
            params.put("out_trade_no", String.valueOf(payResult.getOut_trade_no()));
        if (payResult.getApi_trade_no() != null)
            params.put("api_trade_no", String.valueOf(payResult.getApi_trade_no()));
        if (payResult.getType() != null) params.put("type", payResult.getType());
        if (payResult.getTrade_status() != null) params.put("trade_status", payResult.getTrade_status());
        if (payResult.getAddtime() != null) params.put("addtime", payResult.getAddtime());
        if (payResult.getEndtime() != null) params.put("endtime", payResult.getEndtime());
        if (payResult.getName() != null) params.put("name", payResult.getName());
        if (payResult.getMoney() != null) params.put("money", payResult.getMoney());
        if (payResult.getBuyer() != null) params.put("buyer", payResult.getBuyer());
        if (payResult.getTimestamp() != null) params.put("timestamp", payResult.getTimestamp());
        if (payResult.getSign_type() != null) params.put("sign_type", payResult.getSign_type());

        return payService.checkSign(params, payResult.getSign());

    }

}

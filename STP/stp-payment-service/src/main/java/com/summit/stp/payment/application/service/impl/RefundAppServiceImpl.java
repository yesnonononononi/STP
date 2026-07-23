package com.summit.stp.payment.application.service.impl;

import com.summit.stp.payment.application.command.RefundCommand;
import com.summit.stp.payment.application.service.RefundAppService;
import com.summit.stp.payment.application.vo.RefundResultVO;
import com.summit.stp.shared.exception.BusinessException;
import com.summit.stp.shared.util.PaymentSignHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundAppServiceImpl implements RefundAppService {

    private final RestTemplate restTemplate;
    private final PaymentSignHelper paymentSignHelper;

    @Value("${payment.pid}")
    private Integer pid;

    @Value("${payment.refund_url:https://pay.aikelaikaifa.com/api/pay/refund}")
    private String refundUrl;

    @Override
    public RefundResultVO refund(RefundCommand command) {
        log.info("【发起退款】接收到退款请求指令: {}", command);

        if (command.getTradeNo() == null && command.getOutTradeNo() == null) {
            throw new BusinessException("平台订单号(tradeNo)和商户订单号(outTradeNo)不能同时为空");
        }
        if (command.getMoney() == null) {
            throw new BusinessException("退款金额不能为空");
        }

        try {
            // 1. 构造请求参数Map
            MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
            requestParams.add("pid", String.valueOf(pid));
            
            if (command.getTradeNo() != null && !command.getTradeNo().isEmpty()) {
                requestParams.add("trade_no", command.getTradeNo());
            }
            if (command.getOutTradeNo() != null && !command.getOutTradeNo().isEmpty()) {
                requestParams.add("out_trade_no", command.getOutTradeNo());
            }
            
            // 格式化金额为2位小数的字符串
            String moneyStr = command.getMoney().setScale(2, RoundingMode.HALF_UP).toPlainString();
            requestParams.add("money", moneyStr);

            if (command.getOutRefundNo() != null && !command.getOutRefundNo().isEmpty()) {
                requestParams.add("out_refund_no", command.getOutRefundNo());
            }

            // 2. 自动加签并构建 HttpEntity
            HttpEntity<MultiValueMap<String, String>> requestEntity = paymentSignHelper.buildFormUrlEncodedEntity(requestParams);

            log.info("【发起退款】请求URL: {}, 参数: {}", refundUrl, requestParams);

            // 4. 发送 POST 请求并接收响应
            RefundResultVO response = restTemplate.postForObject(refundUrl, requestEntity, RefundResultVO.class);
            log.info("【发起退款】接收到三方退款响应: {}", response);

            if (response == null) {
                throw new BusinessException("三方退款响应为空，退款失败");
            }

            return response;

        } catch (Exception e) {
            log.error("【发起退款】异常: ", e);
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException("三方退款接口请求异常: " + e.getMessage());
        }
    }


}

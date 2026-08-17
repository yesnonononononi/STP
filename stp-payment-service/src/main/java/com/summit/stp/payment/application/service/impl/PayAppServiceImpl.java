package com.summit.stp.payment.application.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.summit.stp.payment.api.dto.PayCommand;
import com.summit.stp.common.application.domain.event.PayFailEvent;
import com.summit.stp.common.application.domain.event.PaySuccessEvent;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.model.PayType;
import com.summit.stp.payment.api.vo.PayVO;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.util.PaymentSignHelper;
import com.summit.stp.payment.application.command.PayCallbackCheckCommand;
import com.summit.stp.payment.application.dto.GatewayPayResponse;
import com.summit.stp.payment.application.service.PayAppService;
import com.summit.stp.payment.application.service.PayEventPublishProvider;
import com.summit.stp.payment.domain.PayDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class PayAppServiceImpl implements PayAppService {
    private final PayDomainService payService;
    private final PayEventPublishProvider payEventPublishProvider;
    private final RestTemplate restTemplate;
    private final PaymentSignHelper paymentSignHelper;

    @Value("${payment.pid}")
    private Integer pid;

    @Value("${payment.notify_url}")
    private String notifyUrl;

    @Value("${payment.return_url}")
    private String returnUrl;

    @Value("${payment.create_url:https://pay.aikelaikaifa.com/api/pay/create}")
    private String createUrl;

    @Value("${payment.pay.success.symbol}")
    private String successSymbol;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PayVO pay(PayCommand payCommand) {
        log.info("【支付】收到支付请求，参数: {}", payCommand);
        // 1. 构造请求参数
        MultiValueMap<String, String> requestParams = buildRequestParams(payCommand);

        // 2. 调用三方网关获取响应 (获取真实响应内容，适配 text/html 等非标准 JSON 响应类型)
        GatewayPayResponse response = callGateway(requestParams);

        // 3. 校验状态并验证签名
        verifyResponse(response);

        // 4. 构建并返回支付响应
        PayVO resultVo = buildResultVo(payCommand, response);
        log.info("【支付】生成支付响应，结果: {}", resultVo);
        return resultVo;
    }

    /**
     * 步骤方法1：构造三方网关下单请求参数
     */
    private MultiValueMap<String, String> buildRequestParams(PayCommand payCommand) {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("pid", String.valueOf(pid));
        requestParams.add("method", "web");
        requestParams.add("device", "pc");
        requestParams.add("type", payCommand.getPayType().getType());
        requestParams.add("out_trade_no", String.valueOf(payCommand.getOrderId()));
        requestParams.add("notify_url", notifyUrl);
        requestParams.add("return_url", returnUrl + "?orderId=" + payCommand.getOrderId());
        requestParams.add("name", payCommand.getMemberName() != null ? payCommand.getMemberName() : "VIP");
        requestParams.add("money", payCommand.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
        requestParams.add("clientip", "127.0.0.1");
        return requestParams;
    }

    /**
     * 步骤方法2：请求三方支付网关下单接口并解析响应
     */
    private GatewayPayResponse callGateway(MultiValueMap<String, String> requestParams) {
        HttpEntity<MultiValueMap<String, String>> requestEntity = paymentSignHelper.buildFormUrlEncodedEntity(requestParams);
        log.info("【支付】发送三方下单请求，URL: {}, 参数: {}", createUrl, requestParams);

        String responseStr;
        try {
            // 使用 String.class 接收响应，绕过 RestTemplate 对 text/html content-type 的解析错误
            responseStr = restTemplate.postForObject(createUrl, requestEntity, String.class);
        } catch (Exception e) {
            log.error("【支付】请求三方支付网关网络异常: ", e);
            throw new BusinessException("请求三方支付网关异常: " + e.getMessage());
        }

        log.info("【支付】收到三方下单响应原始字符串: {}", responseStr);
        if (responseStr == null || responseStr.trim().isEmpty()) {
            throw new BusinessException("三方下单响应为空，支付失败");
        }

        try {
            return new ObjectMapper().readValue(responseStr, GatewayPayResponse.class);
        } catch (Exception e) {
            log.error("【支付】解析三方响应 JSON 异常: ", e);
            throw new BusinessException("解析三方响应异常");
        }
    }

    /**
     * 步骤方法3：验证三方下单响应的业务状态及签名
     */
    private void verifyResponse(GatewayPayResponse response) {
        if (response.getCode() == null || response.getCode() != 0) {
            log.error("【支付】三方下单失败: code={}, msg={}", response.getCode(), response.getMsg());
            throw new BusinessException("三方下单失败: " + (response.getMsg() != null ? response.getMsg() : "未知错误"));
        }

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("code", String.valueOf(response.getCode()));
        if (response.getTrade_no() != null) responseMap.put("trade_no", response.getTrade_no());
        if (response.getPay_type() != null) responseMap.put("pay_type", response.getPay_type());
        if (response.getPay_info() != null) responseMap.put("pay_info", response.getPay_info());
        if (response.getTimestamp() != null) responseMap.put("timestamp", response.getTimestamp());

        if (!payService.checkSign(responseMap, response.getSign())) {
            log.error("【支付】三方下单响应验签失败: {}", response);
            throw new BusinessException("支付网关响应验签失败");
        }
    }

    /**
     * 步骤方法4：构建支付结果 PayVO 实体
     */
    private PayVO buildResultVo(PayCommand payCommand, GatewayPayResponse response) {
        return PayVO.builder()
                .pid(pid)
                .type(payCommand.getPayType().getType())
                .orderId(payCommand.getOrderId())
                .money(payCommand.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString())
                .memberName(payCommand.getMemberName())
                .to(response.getPay_info())
                .sign(response.getSign())
                .timestamp(response.getTimestamp())
                .signType(response.getSign_type())
                .build();
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


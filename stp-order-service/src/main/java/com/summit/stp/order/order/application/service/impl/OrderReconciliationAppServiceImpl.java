package com.summit.stp.order.order.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.common.application.domain.event.PaySuccessEvent;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.service.queue.QueueSender;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.util.PaymentSignHelper;
import com.summit.stp.order.order.application.service.OrderReconciliationAppService;
import com.summit.stp.order.order.application.vo.PaymentQueryResultVO;
import com.summit.stp.order.order.domain.exception.OrderNotFoundException;
import com.summit.stp.order.order.domain.model.Order;
import com.summit.stp.order.order.domain.model.OrderStatus;
import com.summit.stp.order.order.domain.repository.OrderRepository;
import com.summit.stp.order.order.infrastructure.persistence.mapper.OrderMapper;
import com.summit.stp.order.order.infrastructure.persistence.po.OrderPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderReconciliationAppServiceImpl implements OrderReconciliationAppService {

    private final RestTemplate restTemplate;
    private final PaymentSignHelper paymentSignHelper;
    private final QueueSender queueSender;
    private final OrderRepository orderRepository;

    @Value("${payment.pid}")
    private Integer pid;

    @Value("${payment.query_url:https://pay.aikelaikaifa.com/api/pay/query}")
    private String queryUrl;

    @Override
    public Result<String> reconcileOrder(Long orderId, boolean requireCompensation) {
        log.info("【订单对账】开始对账订单: {}", orderId);

        Order order = orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
        // 已经支付或完成的订单，无需对账更新
        // 本地状态：0-待支付, 1-已支付, 2-已完成, 3-已取消
        if (order.getStatus() != null && (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.COMPLETED)) {
            return Result.success("订单本地已处于完成/支付状态，无需对账。");
        }

        // 1. 调用三方支付查询接口
        PaymentQueryResultVO queryResult = queryThirdPartyOrder(orderId);

        // 2. 判断三方状态
        // 支付状态 status：0-未支付，1-已支付，2-已退款，3-已冻结，4-预授权
        Integer thirdPartyStatus = queryResult.getStatus();
        log.info("【订单对账】三方查询返回状态: {}, 本地状态: {}", thirdPartyStatus, order.getStatus());

        if (thirdPartyStatus != null && thirdPartyStatus == OrderStatus.PAID.getCode()) {
            // 三方已支付，本地未支付/已取消
            log.warn("【订单对账】检测到掉单！三方已支付，本地为待支付/已取消，开始进行状态补偿。订单ID: {}", orderId);

            // 补偿订单状态并分发权益
            if (requireCompensation) {
                compensateOrder(orderId);
            }

            return Result.success("对账发现掉单，已成功触发补偿分发逻辑！");
        } else {
            log.error("三方支付平台查询状态为: {}，订单未支付完成", getThirdPartyStatusDesc(thirdPartyStatus));
            return Result.error("订单未支付完成");
        }
    }

    @Override
    public Result<Void> compensateOrder(Long orderId) {
        queueSender.send(MqConstants.Pay.EXCHANGE, MqConstants.Pay.ROUTING_KEY_SUCCESS, new PaySuccessEvent(orderId));
        return Result.success();
    }

    private PaymentQueryResultVO queryThirdPartyOrder(Long orderId) {
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("pid", String.valueOf(pid));
        requestParams.add("out_trade_no", String.valueOf(orderId));

        HttpEntity<MultiValueMap<String, String>> requestEntity = paymentSignHelper.buildFormUrlEncodedEntity(requestParams);

        PaymentQueryResultVO res = restTemplate.postForObject(queryUrl, requestEntity, PaymentQueryResultVO.class);
        if (res == null || res.getCode() != 0) {
            log.warn("【订单对账】查询三方订单状态失败: {}, 错误信息: {} ", orderId, res);
            throw new BusinessException("查询三方订单状态失败: " + (res != null ?  Objects.toString(res.getMsg(),"") : ""));
        }
        return res;
    }


    private String getThirdPartyStatusDesc(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "未支付";
            case 1 -> "已支付";
            case 2 -> "已退款";
            case 3 -> "已冻结";
            case 4 -> "预授权";
            default -> "未知状态码(" + status + ")";
        };
    }


}

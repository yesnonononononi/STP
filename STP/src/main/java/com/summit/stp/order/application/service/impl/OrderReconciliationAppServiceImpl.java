package com.summit.stp.order.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.order.application.service.OrderReconciliationAppService;
import com.summit.stp.order.application.vo.PaymentQueryResultVO;
import com.summit.stp.order.infrastructure.persistence.mapper.OrderMapper;
import com.summit.stp.order.infrastructure.persistence.po.OrderPO;
import com.summit.stp.payment.domain.event.PaySuccessEvent;
import com.summit.stp.shared.exception.BusinessException;
import com.summit.stp.shared.service.subcribe.api.EventPublisher;
import com.summit.stp.shared.util.EncryptUtil;
import com.summit.stp.shared.util.PaymentSignHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderReconciliationAppServiceImpl implements OrderReconciliationAppService {

    private final RestTemplate restTemplate;
    private final OrderMapper orderMapper;
    private final PaymentSignHelper paymentSignHelper;

    @Value("${payment.pid}")
    private Integer pid;

    @Value("${payment.query_url:https://pay.aikelaikaifa.com/api/pay/query}")
    private String queryUrl;

    @Override
    public String reconcileOrder(Long orderId) {
        log.info("【订单对账】开始对账订单: {}", orderId);
        
        OrderPO orderPO = orderMapper.selectById(orderId);
        if (orderPO == null) {
            throw new BusinessException("对账失败，订单不存在: " + orderId);
        }

        // 已经支付或完成的订单，无需对账更新
        // 本地状态：0-待支付, 1-已支付, 2-已完成, 3-已取消
        if (orderPO.getStatus() != null && (orderPO.getStatus() == 1 || orderPO.getStatus() == 2)) {
            return "订单本地已处于完成/支付状态，无需对账。";
        }

        // 1. 调用三方支付查询接口
        PaymentQueryResultVO queryResult = queryThirdPartyOrder(orderId);
        
        if (queryResult == null || queryResult.getCode() != 0) {
            String msg = queryResult != null ? queryResult.getMsg() : "响应为空";
            log.warn("【订单对账】查询三方订单状态失败: {}, 错误信息: {}", orderId, msg);
            throw new BusinessException("查询三方订单状态失败: " + msg);
        }

        // 2. 判断三方状态
        // 支付状态 status：0-未支付，1-已支付，2-已退款，3-已冻结，4-预授权
        Integer thirdPartyStatus = queryResult.getStatus();
        log.info("【订单对账】三方查询返回状态: {}, 本地状态: {}", thirdPartyStatus, orderPO.getStatus());

        if (thirdPartyStatus != null && thirdPartyStatus == 1) {
            // 三方已支付，本地未支付/已取消
            log.warn("【订单对账】检测到掉单！三方已支付，本地为待支付/已取消，开始进行状态补偿。订单ID: {}", orderId);
            // 补偿订单状态并分发权益
            EventPublisher.publish(new PaySuccessEvent(orderId));
            return "对账发现掉单，已成功触发补偿分发逻辑！";
        } else {
            throw new BusinessException("三方支付平台查询状态为: " + getThirdPartyStatusDesc(thirdPartyStatus) + "，订单未支付完成");
        }
    }

    @Override
    public void reconcilePendingOrders() {
        log.info("【批量对账】开始扫描最近的未支付/已取消订单进行批量对账...");
        
        // 扫描最近24小时内未支付的订单
        LambdaQueryWrapper<OrderPO> queryWrapper = new LambdaQueryWrapper<>();
        // status = 0 (待支付) 或者 status = 3 (已取消，以防超时取消但在渠道侧成功支付的情况)
        queryWrapper.in(OrderPO::getStatus, List.of(0, 3))
                .gt(OrderPO::getCreateTime, new Timestamp(System.currentTimeMillis() - 86400000L)); // 最近1天

        List<OrderPO> pendingOrders = orderMapper.selectList(queryWrapper);
        if (pendingOrders.isEmpty()) {
            log.info("【批量对账】未扫描到需要对账的待支付/已取消订单。");
            return;
        }

        log.info("【批量对账】扫描到 {} 个待对账订单，开始逐个对账...", pendingOrders.size());
        for (OrderPO order : pendingOrders) {
            try {
                String result = reconcileOrder(order.getId());
                log.info("【批量对账】订单 {} 对账结果: {}", order.getId(), result);
            } catch (Exception e) {
                log.error("【批量对账】订单 {} 对账异常: ", order.getId(), e);
            }
        }
        log.info("【批量对账】批量对账结束。");
    }

    private PaymentQueryResultVO queryThirdPartyOrder(Long orderId) {
        try {
            MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
            requestParams.add("pid", String.valueOf(pid));
            requestParams.add("out_trade_no", String.valueOf(orderId));
            
            HttpEntity<MultiValueMap<String, String>> requestEntity = paymentSignHelper.buildFormUrlEncodedEntity(requestParams);

            return restTemplate.postForObject(queryUrl, requestEntity, PaymentQueryResultVO.class);
        } catch (Exception e) {
            log.error("【订单对账】接口请求异常, 订单ID: " + orderId, e);
            return null;
        }
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

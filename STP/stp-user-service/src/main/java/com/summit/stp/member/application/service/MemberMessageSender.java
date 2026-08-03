package com.summit.stp.member.application.service;

import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.common.application.service.queue.QueueSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberMessageSender {
    private final QueueSender queueSender;

    /**
     * 发送会员支付/购买成功事件
     */
    public void sendMemberPay(Object event) {
        queueSender.send(MqConstants.Member.EXCHANGE, MqConstants.Member.ROUTING_KEY, event);
    }
}

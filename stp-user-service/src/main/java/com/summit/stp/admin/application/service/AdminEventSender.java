package com.summit.stp.admin.application.service;

import com.summit.stp.common.application.domain.event.AdminChangedEvent;
import com.summit.stp.common.constants.MqConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
@RequiredArgsConstructor
@Component
public class AdminEventSender {
    private final RabbitTemplate rabbitTemplate;

    public void send(AdminChangedEvent event){
        rabbitTemplate.convertAndSend(MqConstants.Admin.EXCHANGE, MqConstants.Admin.ROUTING_KEY, event);
    }
}

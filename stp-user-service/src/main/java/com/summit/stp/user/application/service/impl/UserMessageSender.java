package com.summit.stp.user.application.service.impl;

import com.summit.stp.common.application.service.queue.QueueSender;
import com.summit.stp.common.constants.MqConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMessageSender {
    private final QueueSender queueSender;

    /**
     * 发送用户注册事件
     */
    public void sendUserRegister(Object event) {
        queueSender.send(MqConstants.User.EXCHANGE, MqConstants.User.ROUTING_KEY_REGISTER, event);
    }

    /**
     * 发送粉丝统计更新事件
     */
    public void sendFansUpdate(Object event) {
        queueSender.send(MqConstants.User.EXCHANGE, MqConstants.User.ROUTING_KEY_FANS, event);
    }

    /**
     * 发送获赞统计更新事件
     */
    public void sendLikedUpdate(Object event) {
        queueSender.send(MqConstants.User.EXCHANGE, MqConstants.User.ROUTING_KEY_LIKED, event);
    }
}

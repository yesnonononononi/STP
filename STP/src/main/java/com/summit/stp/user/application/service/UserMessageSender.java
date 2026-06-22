package com.summit.stp.user.application.service;

import com.summit.stp.shared.constants.MqConstants;
import com.summit.stp.shared.service.queue.QueueSender;
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

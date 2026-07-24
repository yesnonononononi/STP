package com.summit.stp.post.application.service;

import com.summit.stp.shared.application.vo.MessageVO;
import com.summit.stp.shared.domain.event.PostPublishEvent;
import com.summit.stp.shared.domain.event.PostInteractionEvent;
import com.summit.stp.shared.constants.MqConstants;
import com.summit.stp.shared.service.queue.QueueSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostMessageSender {
    private final QueueSender queueSender;


    /**
     * 发送帖子发布事件
     * @param event 帖子发布领域事件
     */
    public void sendPostPublish(PostPublishEvent event) {
        queueSender.send(MqConstants.Post.EXCHANGE, MqConstants.Post.ROUTING_KEY, event);
    }

    /**
     * 发送帖子互动事件 (点赞/收藏)
     * @param event 帖子互动事件
     */
    public void sendPostInteraction(PostInteractionEvent event) {
        queueSender.send(MqConstants.Post.EXCHANGE, MqConstants.Post.ROUTING_KEY_INTERACTION, event);
    }
}

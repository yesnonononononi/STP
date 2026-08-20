package com.summit.stp.post.application.service;

import com.summit.stp.common.application.domain.event.PostInteractionEvent;
import com.summit.stp.common.application.service.queue.QueueSender;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.common.application.domain.event.PostChangeEvent;
import com.summit.stp.elasticsearch.document.PostDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostMessageSender {
    private final QueueSender queueSender;




    /**
     * 发送帖子互动事件 (点赞/收藏)
     * @param event 帖子互动事件
     */
    public void sendPostInteraction(PostInteractionEvent event) {
        queueSender.send(MqConstants.Post.EXCHANGE, MqConstants.Post.ROUTING_KEY_INTERACTION, event);
    }


    /**
     * 发送帖子变更事件
     * @param event 帖子变更事件
     */
    public void sendPostChangeEvent(PostChangeEvent event) {
        queueSender.send(MqConstants.Post.EXCHANGE, MqConstants.Post.ROUTING_KEY_CHANGE, event);
    }

    /**
     * 发送系统 ES 专用帖子更新事件
     * @param event ES 帖子更新事件
     */
    public void sendEsPostUpdateEvent(com.summit.stp.common.application.domain.event.EsPostUpdateEvent event) {
        queueSender.send(MqConstants.Es.EXCHANGE, MqConstants.Es.ROUTING_KEY_POST_UPDATE, event);
    }
}

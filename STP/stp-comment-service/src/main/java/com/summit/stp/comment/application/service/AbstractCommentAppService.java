package com.summit.stp.comment.application.service;

import com.summit.stp.shared.domain.event.CommentNotificationMessage;
import com.summit.stp.shared.constants.MqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractCommentAppService implements CommentAppService {

    @Autowired
    protected RabbitTemplate rabbitTemplate;

    /**
     * 推送评论/回复互动通知到消息队列
     * @param msg 消息数据
     */
    protected void pushCommentNotification(CommentNotificationMessage msg) {
        if (msg == null) {
            return;
        }
        try {
            rabbitTemplate.convertAndSend(MqConstants.Comment.EXCHANGE, MqConstants.Comment.ROUTING_KEY, msg);
            log.info("【评论模块】推送评论消息到 MQ 成功, commentId={}", msg.getCommentId());
        } catch (Exception e) {
            log.error("【评论模块】推送评论消息到 MQ 异常, commentId={}", msg.getCommentId(), e);
        }
    }
}

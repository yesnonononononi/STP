package com.summit.stp.elasticsearch.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.common.application.domain.event.EsPostUpdateEvent;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.elasticsearch.document.PostDocument;

import com.summit.stp.elasticsearch.repo.EsPostRepository;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

/**
 * 系统 ES 专属帖子更新同步监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EsPostUpdateListener {

    private final PostRepository postRepository;
    private final EsPostRepository postDocumentRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Es.QUEUE_POST_UPDATE, durable = "true"),
            exchange = @Exchange(name = MqConstants.Es.EXCHANGE, type = "topic"),
            key = MqConstants.Es.ROUTING_KEY_POST_UPDATE
    ))
    public void handleEsPostUpdate(EsPostUpdateEvent event, Channel channel , Message message) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (event == null || event.getPostId() == null) {
            return;
        }
        log.info("【帖子模块】收到系统ES帖子更新事件，postId={}, eventType={}, status={}",
                event.getPostId(), event.getEventType(), event.getStatus());

        try {
            Long postId = event.getPostId();
            if (event.getEventType() == EsPostUpdateEvent.EventType.DELETE) {
                postDocumentRepository.deleteById(postId);
                log.info("【帖子模块】系统ES删除帖子文档成功，postId={}", postId);
                return;
            }

            Post post = postRepository.findById(postId).orElse(null);
            if (post == null) {
                log.warn("【帖子模块】系统ES同步未找到帖子实体，postId={}", postId);
                return;
            }

            PostDocument doc = PostDocument.builder()
                    .id(post.getId())
                    .postId(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .creatorId(post.getCreatorId())
                    .status(post.getStatus() != null ? post.getStatus().getCode() : 10)
                    .createTime(post.getCreateTime() != null ? Date.from(post.getCreateTime().toInstant()) : null)
                    .updateTime(post.getUpdateTime() != null ? Date.from(post.getUpdateTime().toInstant()) : null)
                    .likeCount(post.getLikeCount())
                    .comment(post.getReplyCount())
                    .build();
            log.info("【帖子模块】系统ES将被覆盖:{}",doc);
            postDocumentRepository.save(doc);
            log.info("【帖子模块】系统ES全量覆盖更新帖子文档成功，postId={}", postId);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【帖子模块】处理系统ES帖子更新事件发生异常，postId={}", event.getPostId(), e);
            throw e;
        }
    }
}

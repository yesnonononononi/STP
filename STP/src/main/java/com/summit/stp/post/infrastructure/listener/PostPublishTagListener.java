package com.summit.stp.post.infrastructure.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.post.domain.event.PostPublishEvent;
import com.summit.stp.post.domain.model.PostTag;
import com.summit.stp.post.domain.repository.PostTagRelRepository;
import com.summit.stp.post.domain.repository.TagRepository;
import com.summit.stp.shared.constants.MqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class PostPublishTagListener {
    private final TagRepository tagRepository;
    private final PostTagRelRepository postTagRelRepository;

    public PostPublishTagListener(TagRepository tagRepository, PostTagRelRepository postTagRelRepository) {
        this.tagRepository = tagRepository;
        this.postTagRelRepository = postTagRelRepository;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Post.QUEUE, durable = "true"),
            exchange = @Exchange(name = MqConstants.Post.EXCHANGE, type = "topic"),
            key = MqConstants.Post.ROUTING_KEY
    ))
    public void listen(PostPublishEvent postPublishEvent, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            channel.basicAck(deliveryTag, false);
            Long postId = postPublishEvent.getPostId();
            //2,获取标签关系
            List<PostTag> postTagRel = postTagRelRepository.findByPostId(postId);
            //3,原子自增更新标签的useCount
            for (PostTag tag : postTagRel) {
                tagRepository.incrementUseCount(tag.getTagId(), 1);
            }
        } catch (Exception e) {
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("【标签使用数更新】更新标签使用数失败", e);
            }
        }
        log.info("【标签使用数更新】更新标签使用数成功");
    }
}

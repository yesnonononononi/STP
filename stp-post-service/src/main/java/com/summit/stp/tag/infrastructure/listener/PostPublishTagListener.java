package com.summit.stp.tag.infrastructure.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.common.application.domain.event.PostChangeEvent;
import com.summit.stp.tag.application.service.TagCacheProvider;
import com.summit.stp.tag.domain.model.PostTag;
import com.summit.stp.tag.domain.repository.PostTagRelRepository;
import com.summit.stp.tag.domain.repository.TagRepository;
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
    private final TagCacheProvider tagCacheProvider;

    public PostPublishTagListener(TagRepository tagRepository, PostTagRelRepository postTagRelRepository, TagCacheProvider tagCacheProvider) {
        this.tagRepository = tagRepository;
        this.postTagRelRepository = postTagRelRepository;
        this.tagCacheProvider = tagCacheProvider;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Post.QUEUE, durable = "true"),
            exchange = @Exchange(name = MqConstants.Post.EXCHANGE, type = "topic"),
            key = MqConstants.Post.ROUTING_KEY_CHANGE
    ))

    public void listen(PostChangeEvent postPublishEvent, Channel channel, Message message) {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if(!postPublishEvent.getEventType().equals(PostChangeEvent.EventType.CREATE)){
                channel.basicAck(deliveryTag, false);
                return;
            }
            Long postId = postPublishEvent.getPostId();
            List<Long> tagIds = postPublishEvent.getTagIds();
            if (tagIds == null || tagIds.isEmpty()) {
                List<PostTag> postTagRel = postTagRelRepository.findByPostId(postId);
                if (postTagRel != null && !postTagRel.isEmpty()) {
                    tagIds = postTagRel.stream().map(PostTag::getTagId).toList();
                }
            }

            if (tagIds != null && !tagIds.isEmpty()) {
                // 3. 原子自增更新标签在 DB 中的 useCount
                for (Long tagId : tagIds) {
                    tagRepository.incrementUseCount(tagId, 1);
                }
                // 4. 异步加入 ZSet 标签列表缓存
                tagCacheProvider.addPostToTags(postId, tagIds);
                log.info("【标签模块】发帖事件消费成功, postId={}, tagIds={}", postId, tagIds);
            }
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("【标签模块】MQ应答失败", ioException);
            }
            log.error("【标签模块】异步处理发帖标签更新失败, postId={}", postPublishEvent.getPostId(), e);
        }
    }
}

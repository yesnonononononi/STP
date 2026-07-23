package com.summit.stp.rank_board.infrastructure.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.shared.domain.event.PostPublishEvent;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.model.PostTag;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.domain.repository.PostTagRelRepository;
import com.summit.stp.rank_board.application.service.RankCacheProvider;
import com.summit.stp.shared.constants.MqConstants;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class RankPostPublishListener {
    private final PostRepository postRepository;
    private final PostTagRelRepository postTagRelRepository;
    private final RankCacheProvider rankCacheProvider;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Rank.QUEUE_POST_PUBLISH, durable = "true"),
            exchange = @Exchange(name = MqConstants.Post.EXCHANGE, type = "topic"),
            key = MqConstants.Post.ROUTING_KEY
    ))
    public void listen(PostPublishEvent postPublishEvent, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            channel.basicAck(deliveryTag, false);
            Long postId = postPublishEvent.getPostId();
            log.info("【排行榜实时更新】接收到发帖事件: postId={}", postId);
            
            // 1. 初始化帖子热度值到 ZSet
            Post post = postRepository.findById(postId);
            if (post != null) {
                double score = post.calculateHotScore();
                rankCacheProvider.cachePostScore(postId, score);
                log.info("【排行榜实时更新】已初始化帖子热度, postId={}, score={}", postId, score);
            }
            
            // 2. 将标签的使用次数增加 1
            List<PostTag> postTagRel = postTagRelRepository.findByPostId(postId);
            for (PostTag tag : postTagRel) {
                rankCacheProvider.incrementTopicScore(tag.getTagId(), 1.0);
                log.info("【排行榜实时更新】自增标签使用次数, tagId={}", tag.getTagId());
            }
        } catch (Exception e) {
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("【排行榜实时更新】MQ应答失败", ioException);
            }
            log.error("【排行榜实时更新】处理发帖实时更新失败: postId={}", postPublishEvent.getPostId(), e);
        }
    }
}

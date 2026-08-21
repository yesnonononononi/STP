package com.summit.stp.rank_board.infrastructure.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.common.application.domain.event.PostChangeEvent;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.rank_board.application.service.CreatorRankBufferManager;
import com.summit.stp.rank_board.application.service.RankCacheProvider;
import com.summit.stp.tag.domain.model.PostTag;
import com.summit.stp.tag.domain.repository.PostTagRelRepository;
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
    private final CreatorRankBufferManager creatorRankBufferManager;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Rank.QUEUE_POST_PUBLISH, durable = "true"),
            exchange = @Exchange(name = MqConstants.Post.EXCHANGE, type = "topic"),
            key = MqConstants.Post.ROUTING_KEY_CHANGE
    ))

    public void listen(PostChangeEvent postPublishEvent, Channel channel, Message message) throws IOException {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if(!postPublishEvent.getEventType().equals(PostChangeEvent.EventType.CREATE)){
                channel.basicAck(deliveryTag, false);
                return;
            }

            Long postId = postPublishEvent.getPostId();
            log.info("【排行榜实时更新】接收到发帖事件: postId={}", postId);
            
            // 1. 初始化帖子热度值到 ZSet
            Post post = postRepository.findById(postId).orElse(null);
            if (post != null) {
                double score = post.calculateHotScore();
                rankCacheProvider.cachePostScore(postId, score);
                log.info("【排行榜实时更新】已初始化帖子热度, postId={}, score={}", postId, score);
                
                if (post.getCreatorId() != null) {
                    creatorRankBufferManager.incrementScore(post.getCreatorId(), PostConstants.Business.CREATOR_SCORE_POST);
                    log.info("【排行榜实时更新】已自增作者发帖积分, userId={}", post.getCreatorId());
                }
            }
            
            // 2. 将标签的使用次数增加 1
            List<Long> tagIds = postPublishEvent.getTagIds();
            if (tagIds == null || tagIds.isEmpty()) {
                List<PostTag> postTagRel = postTagRelRepository.findByPostId(postId);
                tagIds = postTagRel.stream().map(PostTag::getTagId).toList();
            }
            for (Long tagId : tagIds) {
                rankCacheProvider.incrementTopicScore(tagId, 1.0);
                log.info("【排行榜实时更新】自增标签使用次数, tagId={}", tagId);
            }
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("【排行榜实时更新】处理发帖实时更新失败: postId={}", postPublishEvent.getPostId(), e);
            throw e;

        }
    }
}

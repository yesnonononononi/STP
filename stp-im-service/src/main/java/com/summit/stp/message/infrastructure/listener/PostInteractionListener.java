package com.summit.stp.message.infrastructure.listener;

import cn.hutool.core.util.IdUtil;
import com.rabbitmq.client.Channel;
import com.summit.stp.common.application.domain.event.PostInteractionEvent;
import com.summit.stp.common.application.service.TextSafe.TextSafeServiceProvider;
import com.summit.stp.common.application.api.vo.PostSimpleVO;
import com.summit.stp.common.application.api.vo.UserSimpleVO;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.common.feign.PostFeignClient;
import com.summit.stp.common.feign.UserFeignClient;
import com.summit.stp.message.domain.model.InteractionMessage;
import com.summit.stp.message.domain.model.InteractionMessageType;
import com.summit.stp.message.domain.repository.InteractionMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostInteractionListener {

    private final InteractionMessageRepository interactionMessageRepository;
    private final PostFeignClient postFeignClient;
    private final UserFeignClient userFeignClient;
    private final TextSafeServiceProvider textSafeServiceProvider;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Post.QUEUE_INTERACTION, durable = "true"),
            exchange = @Exchange(name = MqConstants.Post.EXCHANGE, type = "topic"),
            key = MqConstants.Post.ROUTING_KEY_INTERACTION
    ))
    public void onPostInteraction(PostInteractionEvent event, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("【消息模块】监听到帖子互动 MQ 消息，postId={}, type={}", event.getPostId(), event.getInteractionType());
        try {
            Long postId = event.getPostId();
            Long userId = event.getUserId();

            PostSimpleVO post = postFeignClient.findSimplePostById(postId).getData();
            if (post == null) {
                log.warn("【消息模块】未找到帖子：postId={}", postId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 自己点赞/收藏自己的帖子，不产生消息
            if (Objects.equals(post.getCreatorId(), userId)) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            UserSimpleVO user = userFeignClient.findSimpleUserById(userId).getData();
            String nickname = user != null ? user.getNick() : "匿名用户";
            String avatar = user != null ? user.getAvatar() : "";

            // 进行 XSS 字符转义，防止意外攻击
            String safeNickname = textSafeServiceProvider.xssFilter(nickname);

            int messageType = "LIKE".equals(event.getInteractionType())
                    ? InteractionMessageType.LIKE.getCode()
                    : InteractionMessageType.COLLECT.getCode();

            InteractionMessage interactMsg = InteractionMessage.builder()
                    .publicId(IdUtil.getSnowflakeNextId())
                    .senderId(userId)
                    .senderAvatar(avatar)
                    .senderName(safeNickname)
                    .receiverId(post.getCreatorId())
                    .messageType(messageType)
                    .associateContent(postId)
                    .postId(postId)
                    .associateTitle(post.getTitle())
                    .isDel(0)
                    .build();

            interactionMessageRepository.save(interactMsg);
            log.info("【消息模块】保存互动消息：publicId={}", interactMsg.getPublicId());

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【消息模块】处理帖子互动 MQ 消息失败，postId={}", event.getPostId(), e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("【消息模块】MQ基本拒签操作失败", ioException);
            }
        }
    }
}

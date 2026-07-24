package com.summit.stp.message.infrastructure.listener;

import cn.hutool.core.util.IdUtil;
import com.rabbitmq.client.Channel;
import com.summit.stp.shared.domain.event.CommentNotificationMessage;
import com.summit.stp.shared.application.vo.CommentSimpleVO;
import com.summit.stp.message.domain.model.InteractionMessage;
import com.summit.stp.message.domain.model.InteractionMessageType;
import com.summit.stp.message.domain.repository.InteractionMessageRepository;
import com.summit.stp.message.infrastructure.constants.ImConstants;
import com.summit.stp.shared.constants.MqConstants;
import com.summit.stp.shared.service.TextSafe.TextSafeServiceProvider;
import com.summit.stp.common.feign.UserFeignClient;
import com.summit.stp.common.feign.CommentFeignClient;
import com.summit.stp.shared.application.vo.UserSimpleVO;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentNotificationListener {

    private final InteractionMessageRepository interactionMessageRepository;
    private final CommentFeignClient commentFeignClient;
    private final UserFeignClient userFeignClient;
    private final TextSafeServiceProvider textSafeServiceProvider;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Comment.QUEUE, durable = "true"),
            exchange = @Exchange(name = MqConstants.Comment.EXCHANGE, type = "topic"),
            key = MqConstants.Comment.ROUTING_KEY
    ))
    public void onCommentNotification(CommentNotificationMessage msg, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("【消息模块】监听到评论 MQ 消息，开始处理通知落地，commentId={}", msg.getCommentId());
        try {
            // 1. 构造通知的上下文
            NotificationContext context = buildNotificationContext(msg);

            // 2. 场景一：处理对被回复评论发布者的通知
            Long notifyParentUserId = notifyParentCommentPublisher(msg, context);

            // 3. 场景二：处理对帖子发布者的通知
            notifyPostCreator(msg, context, notifyParentUserId);

            // 手动 Ack 应答
            channel.basicAck(deliveryTag, false);
            log.info("【消息模块】评论 MQ 通知落地成功，commentId={}", msg.getCommentId());
        } catch (Exception e) {
            log.error("【消息模块】处理评论通知 MQ 失败，commentId={}", msg.getCommentId(), e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("【消息模块】MQ基本拒签操作失败", ioException);
            }
        }
    }

    /**
     * 场景一：通知被回复评论发布者 (回复消息)
     */
    private Long notifyParentCommentPublisher(CommentNotificationMessage msg, NotificationContext context) {
        if (msg.getParentId() == null) {
            return null;
        }
        CommentSimpleVO parentComment = commentFeignClient.findSimpleCommentById(msg.getParentId()).getData();
        if (parentComment == null) {
            return null;
        }

        Long notifyParentUserId = parentComment.getPublisherId();
        Long currentUserId = msg.getTriggerUserId();

        // 自己回复自己，不触发通知
        if (Objects.equals(notifyParentUserId, currentUserId)) {
            return notifyParentUserId;
        }

        String associateTitle = buildContent(parentComment);

        InteractionMessage interactMsg = InteractionMessage.builder()
                .publicId(IdUtil.getSnowflakeNextId())
                .senderId(currentUserId)
                .senderAvatar(context.getAvatar())
                .senderName(context.getSafeNickname())
                .receiverId(notifyParentUserId)
                .messageType(InteractionMessageType.REPLY.getCode())
                .content(msg.getCommentContent())
                .associateContent(msg.getCommentId()) // 关联新增的评论，以便查询时丰富帖子和父评论上下文
                .postId(msg.getPostId())
                .associateTitle(associateTitle)
                .isDel(0)
                .build();

        interactionMessageRepository.save(interactMsg);
        return notifyParentUserId;
    }

    private static String buildContent(CommentSimpleVO parentComment) {
        String associateTitle = parentComment.getContent();
        if (parentComment.getType() != null) {
            if (parentComment.getType() == 2) {
                associateTitle = "[图片] " + (associateTitle != null ? associateTitle : "");
            } else if (parentComment.getType() == 3) {
                associateTitle = "[视频] " + (associateTitle != null ? associateTitle : "");
            } else if (parentComment.getType() == 4) {
                associateTitle = "[音频] " + (associateTitle != null ? associateTitle : "");
            }
        }
        return associateTitle;
    }

    /**
     * 场景二：通知帖子发布者 (普通评论消息)
     */
    private void notifyPostCreator(CommentNotificationMessage msg, NotificationContext context, Long notifyParentUserId) {
        Long postCreatorId = msg.getPostCreatorId();
        Long currentUserId = msg.getTriggerUserId();

        if (Objects.equals(postCreatorId, currentUserId) || Objects.equals(postCreatorId, notifyParentUserId)) {
            return;
        }

        InteractionMessage interactMsg = InteractionMessage.builder()
                .publicId(IdUtil.getSnowflakeNextId())
                .senderId(currentUserId)
                .senderAvatar(context.getAvatar())
                .senderName(context.getSafeNickname())
                .receiverId(postCreatorId)
                .messageType(InteractionMessageType.COMMENT.getCode())
                .content(msg.getCommentContent())
                .associateContent(msg.getCommentId())
                .postId(msg.getPostId())
                .associateTitle(msg.getPostTitle())
                .isDel(0)
                .build();

        interactionMessageRepository.save(interactMsg);
    }

    /**
     * 构造并提取通知上下文
     */
    private NotificationContext buildNotificationContext(CommentNotificationMessage msg) {
        Long currentUserId = msg.getTriggerUserId();
        UserSimpleVO publisherVO = userFeignClient.findSimpleUserById(currentUserId).getData();
        String nickname = publisherVO != null && publisherVO.getNick() != null ? publisherVO.getNick() : "匿名用户";
        String safeNickname = textSafeServiceProvider.xssFilter(nickname);
        String avatar = publisherVO != null ? publisherVO.getAvatar() : "";

        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(msg.getTimestamp()), ZoneId.systemDefault());
        String timeStr = localDateTime.format(TIME_FORMATTER);

        String postTitle = msg.getPostTitle() != null ? msg.getPostTitle() : "";
        String shortPostTitle = postTitle.length() > 15 ? postTitle.substring(0, 15) + "..." : postTitle;
        String safePostTitle = HtmlUtils.htmlEscape(shortPostTitle);

        return NotificationContext.builder()
                .safeNickname(safeNickname)
                .avatar(avatar)
                .timeStr(timeStr)
                .safePostTitle(safePostTitle)
                .build();
    }


    @Data
    @Builder
    private static class NotificationContext {
        private String safeNickname;
        private String avatar;
        private String timeStr;
        private String safePostTitle;
    }
}

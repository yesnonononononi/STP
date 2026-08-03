package com.summit.stp.message.application.service.impl;

import com.summit.stp.common.ThreadContext.UserHolder;
import com.summit.stp.common.application.api.vo.CommentSimpleVO;
import com.summit.stp.common.feign.CommentFeignClient;
import com.summit.stp.common.result.Result;
import com.summit.stp.message.application.service.InteractionMessageService;
import com.summit.stp.message.application.vo.InteractionMessageVO;
import com.summit.stp.message.domain.model.InteractionMessage;
import com.summit.stp.message.domain.model.InteractionMessageType;
import com.summit.stp.message.domain.repository.InteractionMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionMessageServiceImpl implements InteractionMessageService {

    private final InteractionMessageRepository interactionMessageRepository;
    private final CommentFeignClient commentFeignClient;

    @Override
    public void saveMessage(InteractionMessage msg) {
        interactionMessageRepository.save(msg);
    }

    @Override
    public List<InteractionMessageVO> getInteractionList(Long lastPublicId, Integer limit) {
        Long currentUserId = UserHolder.getUser().getId();
        List<InteractionMessage> messages = interactionMessageRepository.getInteractionMessages(currentUserId, lastPublicId, limit);
        if (messages.isEmpty()) {
            return List.of();
        }

        // 批量查询所有评论/回复类型消息关联的评论数据
        Map<Long, CommentSimpleVO> commentMap = batchQueryComments(messages);

        List<InteractionMessageVO> voList = new ArrayList<>(messages.size());
        for (InteractionMessage msg : messages) {
            InteractionMessageVO vo = buildVo(msg, msg.getAssociateTitle(), msg.getPostId());
            enrichCommentData(msg, vo, commentMap);
            voList.add(vo);
        }

        return voList;
    }

    /**
     * 批量查询消息列表中所有评论/回复类型的关联评论，返回 commentId -> CommentSimpleVO 映射。
     */
    private Map<Long, CommentSimpleVO> batchQueryComments(List<InteractionMessage> messages) {
        List<Long> commentIds = messages.stream()
                .filter(m -> InteractionMessageType.COMMENT.getCode().equals(m.getMessageType())
                        || InteractionMessageType.REPLY.getCode().equals(m.getMessageType()))
                .map(InteractionMessage::getAssociateContent)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (commentIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            Result<List<CommentSimpleVO>> result = commentFeignClient.findSimpleCommentsByIds(commentIds);
            if (result != null && result.getData() != null) {
                return result.getData().stream()
                        .collect(Collectors.toMap(CommentSimpleVO::getId, c -> c));
            }
        } catch (Exception e) {
            log.warn("批量查询评论失败, commentIds={}", commentIds, e);
        }
        return Collections.emptyMap();
    }

    /**
     * 对评论/回复类型的互动消息，使用批量查询结果填充 VO 的 content 和 isLike。
     * 若评论已删除或查询失败，则降级使用消息自带的 content，isLike 默认 false。
     */
    private void enrichCommentData(InteractionMessage msg, InteractionMessageVO vo,
                                   Map<Long, CommentSimpleVO> commentMap) {
        Integer messageType = msg.getMessageType();
        if (!InteractionMessageType.COMMENT.getCode().equals(messageType)
                && !InteractionMessageType.REPLY.getCode().equals(messageType)) {
            return;
        }

        Long commentId = msg.getAssociateContent();
        if (commentId == null) {
            vo.setIsLike(false);
            return;
        }

        CommentSimpleVO comment = commentMap.get(commentId);
        if (comment != null) {
            vo.setContent(comment.getContent());
            vo.setIsLike(comment.getIsLiked() != null ? comment.getIsLiked() : false);
        } else {
            // 评论已被删除或不存在，降级使用消息自带的 content，isLike 默认 false
            vo.setIsLike(false);
        }
    }

    private InteractionMessageVO buildVo(InteractionMessage msg, String associateContentTitle, Long realPostId) {
        return InteractionMessageVO.builder()
                .publicId(msg.getPublicId())
                .senderId(msg.getSenderId())
                .senderAvatar(msg.getSenderAvatar())
                .senderName(msg.getSenderName())
                .receiverId(msg.getReceiverId())
                .messageType(msg.getMessageType())
                .content(msg.getContent())
                .associateContent(msg.getAssociateContent())
                .postId(realPostId)
                .createTime(msg.getCreateTime())
                .associateContentTitle(associateContentTitle)
                .build();
    }

    @Override
    public void deleteInteractionMessage(Long publicId) {
        Long currentUserId = UserHolder.getUser().getId();
        interactionMessageRepository.deleteByUuid(publicId, currentUserId);
    }
}

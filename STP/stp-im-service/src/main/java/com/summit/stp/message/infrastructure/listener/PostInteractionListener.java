package com.summit.stp.message.infrastructure.listener;

import com.summit.stp.shared.service.TextSafe.TextSafeServiceProvider;
import org.springframework.web.util.HtmlUtils;
import cn.hutool.core.util.IdUtil;
import com.summit.stp.message.domain.model.SystemMessage;
import com.summit.stp.message.domain.model.SystemMessageType;
import com.summit.stp.message.domain.repository.SystemMessageRepository;
import com.summit.stp.shared.domain.event.PostInteractionEvent;
import com.summit.stp.shared.application.vo.PostSimpleVO;
import com.summit.stp.message.infrastructure.constants.ImConstants;
import com.summit.stp.common.feign.UserFeignClient;
import com.summit.stp.common.feign.PostFeignClient;
import com.summit.stp.shared.application.vo.UserSimpleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostInteractionListener {

    private final SystemMessageRepository systemMessageRepository;
    private final PostFeignClient postFeignClient;
    private final UserFeignClient userFeignClient;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final TextSafeServiceProvider textSafeServiceProvider;

    @EventListener
    public void onPostInteraction(PostInteractionEvent event) {
        log.info("【消息模块】监听到互动事件");

        Long postId = event.getPostId();
        Long userId = event.getUserId();

        PostSimpleVO post = postFeignClient.findSimplePostById(postId).getData();
        if (post == null) {
            log.warn("【消息模块】未找到帖子：postId={}", postId);
            return;
        }

        // 自己点赞/收藏自己的帖子，不产生消息
        if (Objects.equals(post.getCreatorId(), userId)) {
            return;
        }

        UserSimpleVO user = userFeignClient.findSimpleUserById(userId).getData();
        String nickname = user != null ? user.getNick() : "匿名用户";
        String avatar = user != null ? user.getAvatar() : "";

        // 进行 XSS 字符转义，防止意外攻击
        String safeNickname = textSafeServiceProvider.xssFilter(nickname);

        LocalDateTime localDateTime = LocalDateTime.ofInstant(event.getTimestamp(), ZoneId.systemDefault());
        String timeStr = localDateTime.format(TIME_FORMATTER);

        String title = post.getTitle();
        String shortTitle = title != null && title.length() > 15 ? title.substring(0, 15) + "..." : (title != null ? title : "");
        String safeTitle = HtmlUtils.htmlEscape(shortTitle);

        String actionStr = "LIKE".equals(event.getInteractionType()) ? "点赞" : "收藏";

        String htmlContent = String.format(
                ImConstants.Business.TEMPLATE_HTML_MSG,
                avatar,
                safeNickname,
                actionStr,
                timeStr,
                safeTitle
        );

        SystemMessage sysMsg = SystemMessage.builder()
                .id(IdUtil.getSnowflakeNextId())
                .fromUserId(userId)
                .content(htmlContent)
                .status(1)
                .associateUser(post.getCreatorId())
                .type(SystemMessageType.PERSONAL.getCode())
                .publicTime(Instant.now())
                .createTime(Instant.now())
                .updateTime(Instant.now())
                .build();

        systemMessageRepository.save(sysMsg);
        log.info("【消息模块】保存系统消息：sysMsgId={}", sysMsg.getId());
    }
}

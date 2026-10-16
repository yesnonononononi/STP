package com.summit.stp.admin.application.service.impl;

import com.summit.stp.admin.application.command.AdminPostQueryCommand;
import com.summit.stp.admin.application.service.AdminPostService;
import com.summit.stp.admin.application.vo.AdminPostVO;
import com.summit.stp.common.application.api.result.ESPageVO;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.application.domain.event.EsPostUpdateEvent;
import com.summit.stp.common.application.domain.event.PostChangeEvent;
import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.elasticsearch.service.PostQuerySupport;
import com.summit.stp.post.application.service.PostAppService;
import com.summit.stp.post.application.service.PostMessageSender;
import com.summit.stp.post.application.service.PostQueryService;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.domain.exception.NoSuchPostException;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.model.PostImage;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPostServiceImpl implements AdminPostService {

    /** 管理端写操作分布式锁等待时间（秒） */
    private static final long LOCK_WAIT_SECONDS = 3L;
    /** 管理端写操作分布式锁租约时间（秒） */
    private static final long LOCK_LEASE_SECONDS = 10L;

    private final PostQuerySupport postQuerySupport;
    private final PostRepository postRepository;
    private final PostAppService postAppService;
    private final TransactionTemplate transactionTemplate;
    private final PostQueryService postQueryService;
    private final PostMessageSender postMessageSender;
    private final DistributedLockUtil distributedLockUtil;

    @Override
    public Result<PageResult<List<AdminPostVO>>> list(AdminPostQueryCommand command) {
        ESPageVO<Long> res = postQuerySupport.listBy(command);
        List<Long> ids = res.getData();
        // 列表为空时返回空页而非 null（#19），保持分页语义自洽
        if (ids == null || ids.isEmpty()) {
            return Result.success(PageResult.empty());
        }
        // total 以 ES 结果集为准（分页总数权威值）；记录回表 IN 查询不保序，故按 ES 返回的 id 顺序重排（#19）
        Map<Long, Post> byId = postRepository.findByIds(ids).stream()
                .collect(Collectors.toMap(Post::getId, p -> p, (a, b) -> a));
        List<AdminPostVO> data = ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(this::toVO)
                .toList();
        return Result.success(new PageResult<>(res.getPage(), res.getTotal(), data));
    }


    @Override
    public void bypass(Long id) {
        // 统一使用 TransactionTemplate 管理事务（不再与 @Transactional 混用，#33），并加分布式锁防重（#24）
        distributedLockUtil.executeWithLock("admin:post:bypass:" + id, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            transactionTemplate.executeWithoutResult(status -> {
                // 1, 查找帖子并更新
                Post post = postRepository.findById(id).orElseThrow(NoSuchPostException::new);
                post.pass();
                postRepository.update(post);
                // 2. 初始化帖子缓存
                postAppService.initPostCache(post);

                // 3, 发布帖子发布事件
                postAppService.publishPostEvent(post, PostChangeEvent.EventType.CREATE, null, id);
            });

            // 事务提交后发布系统 ES 状态变更事件
            publishEsStatusChangeEvent(id, PostStatus.NORMAL.getCode(), null);
        });
    }

    @Override
    public void bypassNot(Long id, String reason) {
        distributedLockUtil.executeWithLock("admin:post:bypassNot:" + id, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            transactionTemplate.executeWithoutResult(status -> {
                Post post = postRepository.findById(id).orElseThrow(NoSuchPostException::new);
                post.unpass(reason);
                postRepository.update(post);
            });

            // 发布系统 ES 状态变更事件
            publishEsStatusChangeEvent(id, PostStatus.UNPASS.getCode(), reason);
        });
    }

    @Override
    public void toggleBan(Long id, boolean attemptBan) {
        distributedLockUtil.executeWithLock("admin:post:ban:" + id, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            Integer[] statusHolder = new Integer[1];
            transactionTemplate.executeWithoutResult(status -> {
                Post post = postRepository.findById(id).orElseThrow(NoSuchPostException::new);
                if (attemptBan) {
                    post.ban();
                    postAppService.deletePostExtraInfo(post, null);
                } else {
                    post.unban();
                }
                postRepository.update(post);
                statusHolder[0] = post.getStatus().getCode();
            });

            // 发布系统 ES 状态变更事件
            publishEsStatusChangeEvent(id, statusHolder[0], null);
        });
    }

    private void publishEsStatusChangeEvent(Long id, Integer status, String unpassReason) {
        try {
            EsPostUpdateEvent esEvent = EsPostUpdateEvent.builder()
                    .postId(id)
                    .eventType(EsPostUpdateEvent.EventType.STATUS_CHANGE)
                    .status(status)
                    .unpassReason(unpassReason)
                    .timestamp(System.currentTimeMillis())
                    .build();
            postMessageSender.sendEsPostUpdateEvent(esEvent);
        } catch (Exception e) {
            log.warn("【管理员帖子模块】发送系统ES状态更新事件异常，postId={}", id, e);
        }
    }

    @Override
    public PostVO getPostById(Long id, Integer status){
        return postQueryService.findById(id, null, status);
    }

    private AdminPostVO toVO(Post post) {
        List<PostImage> urls = post.getUrls();
        return AdminPostVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .type(post.getType().getCode())
                .status(post.getStatus().getCode())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .creatorId(post.getCreatorId())
                .content(post.getContent())
                .mediaUrls(post.getMediaUrls())
                .urls(urls == null ? null :urls.stream().map(PostImage::getImageUrl).toList())
                .replyCount(post.getReplyCount())
                .isTop(post.getIsTop())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .collectCount(post.getCollectCount())
                .hotScore(post.getHotScore())
                .visibleScope(post.getVisibleScope().getCode())
                .build();
    }



}

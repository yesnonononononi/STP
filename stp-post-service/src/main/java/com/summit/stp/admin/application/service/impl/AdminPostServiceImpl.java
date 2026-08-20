package com.summit.stp.admin.application.service.impl;

import com.summit.stp.admin.application.command.AdminPostQueryCommand;
import com.summit.stp.admin.application.service.AdminPostService;
import com.summit.stp.admin.application.vo.AdminPostVO;
import com.summit.stp.common.application.api.result.ESPageVO;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.application.domain.event.EsPostUpdateEvent;
import com.summit.stp.common.application.domain.event.PostChangeEvent;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPostServiceImpl implements AdminPostService {


    private final PostQuerySupport postQuerySupport;
    private final PostRepository postRepository;
    private final PostAppService postAppService;
    private final TransactionTemplate transactionTemplate;
    private final PostQueryService postQueryService;
    private final PostMessageSender postMessageSender;

    @Override
    public Result<PageResult<List<AdminPostVO>>> list(AdminPostQueryCommand command) {
        ESPageVO<Long> res = postQuerySupport.listBy(command);
        List<Long> ids = res.getData();
        List<Post> list = postRepository.findByIds(ids);
        return Result.success(new PageResult<>(res.getPage(), res.getTotal(), list.stream().map(this::toVO).toList()));
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void bypass(Long id) {
        Post p = transactionTemplate.execute(status -> {
            // 1, 查找帖子并更新
            Post post = postRepository.findById(id).orElseThrow(NoSuchPostException::new);
            post.pass();
            postRepository.update(post);
            // 2. 初始化帖子缓存
            postAppService.initPostCache(post);

            // 3, 发布帖子发布事件
            postAppService.publishPostEvent(post, PostChangeEvent.EventType.CREATE, null, id);
            return post;
        });

        // 3. 发布系统 ES 状态变更事件
        publishEsStatusChangeEvent(id, PostStatus.NORMAL.getCode(), null);
    }

    @Override
    public void bypassNot(Long id, String reason) {
        Post post = postRepository.findById(id).orElseThrow(NoSuchPostException::new);
        post.unpass(reason);
        postRepository.update(post);

        // 发布系统 ES 状态变更事件
        publishEsStatusChangeEvent(id, PostStatus.UNPASS.getCode(), reason);
    }

    @Override
    public void toggleBan(Long id, boolean attemptBan) {
        Post post = postRepository.findById(id).orElseThrow(NoSuchPostException::new);
        if (attemptBan) {
            post.ban();
            postAppService.deletePostExtraInfo(post,null);
        } else {
            post.unban();
        }
        postRepository.update(post);

        // 发布系统 ES 状态变更事件
        publishEsStatusChangeEvent(id, post.getStatus().getCode(), null);
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

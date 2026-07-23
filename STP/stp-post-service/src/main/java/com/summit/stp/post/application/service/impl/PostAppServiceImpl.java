package com.summit.stp.post.application.service.impl;


import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.context.ApplicationEventPublisher;
import com.summit.stp.shared.domain.event.PostInteractionEvent;
import com.summit.stp.shared.application.vo.MessageVO;
import com.summit.stp.shared.application.vo.SysMessageVO;
import com.summit.stp.post.api.dto.request.ImageInfo;
import com.summit.stp.post.application.command.CreatePostCommand;
import com.summit.stp.post.application.command.QueryPostListByCursorCommand;
import com.summit.stp.post.application.command.UpdatePostCommand;
import com.summit.stp.post.application.service.*;
import com.summit.stp.post.application.service.impl.cache.InteractionType;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.shared.domain.event.PostPublishEvent;
import com.summit.stp.post.domain.model.*;
import com.summit.stp.post.domain.repository.PostCollectRepository;
import com.summit.stp.post.domain.repository.PostImageRepository;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.domain.repository.TagRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostCollectPO;
import com.summit.stp.post.infrastructure.persistence.po.PostLikePO;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.shared.exception.BusinessException;
import com.summit.stp.shared.exception.ParameterException;
import com.summit.stp.shared.result.Result;
import com.summit.stp.shared.service.TextSafe.TextSafeServiceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class PostAppServiceImpl implements PostAppService {
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final TextSafeServiceProvider textSafeServiceProvider;
    private final PostQueryService postQueryService;
    private final PostTagRelAppService postTagRelAppService;
    private final PostCacheProvider postCacheProvider;
    private final PostMessageSender postMessageSender;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final PostsMapper postsMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TagRepository tagRepository;
    private final com.summit.stp.post.domain.repository.PostTagRelRepository postTagRelRepository;
    private final TagCacheProvider tagCacheProvider;

    @Override
    public PostVO getPostById(Long id) {
        return postQueryService.findById(id, UserHolder.getUser().getId(), PostStatus.NORMAL.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createPost(CreatePostCommand command) {
        validatePostForCreate(command);

        // 1. 构建并保存帖子主体
        Post post = buildNewPost(command);
        post = postRepository.save(post);

        Long postId = post.getId();

        // 2. 保存帖子图片关联
        savePostImages(post, command.getMediaUrls());

        // 3. 绑定帖子标签
        List<Long> actualTagIds = getTagIdsByUuids(command.getTagIds());
        bindPostTags(postId, actualTagIds);

        // 4. 初始化帖子缓存
        initPostCache(post);

        // 5. 发布帖子发布事件
        publishPostEvent(post);

        return Result.success();
    }

    private Post buildNewPost(CreatePostCommand command) {
        PostStatus status = PostStatus.fromCode(command.getStatus());
        List<ImageInfo> mediaUrls = command.getMediaUrls();
        String mediaUrlsStr = "";
        if (mediaUrls != null) {
            if (PostType.IMAGE.getCode() != command.getType() && PostType.TEXT.getCode() != command.getType()) {
                mediaUrlsStr = mediaUrls.stream()
                        .map(ImageInfo::getUrl)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse("");
            }
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Long userId = UserHolder.getUser().getId();
        Long postId = IdUtil.getSnowflakeNextId();
        return Post.builder()
                .id(postId)
                .creatorId(userId)
                .title(command.getTitle())
                .type(PostType.fromCode(command.getType()))
                .content(textSafeServiceProvider.xssFilter(command.getContent()))
                .mediaUrls(mediaUrlsStr)
                .visibleScope(Post.VisibleScope.PUBLIC)
                .hotScore(0.0)
                .replyCount(0L)
                .likeCount(0L)
                .viewCount(0L)
                .collectCount(0L)
                .status(status)
                .createTime(now)
                .updateTime(now)
                .isTop(command.getIsTop() == null ? 0 : command.getIsTop())
                .build();
    }

    private void savePostImages(Post post, List<ImageInfo> mediaUrls) {
        if (post.isImage() && mediaUrls != null && !mediaUrls.isEmpty()) {
            List<PostImage> urls = buildImages(post.getId(), mediaUrls);
            postImageRepository.batchSave(urls);
        }
    }

    private void bindPostTags(Long postId, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            postTagRelAppService.bindTag(postId, tagIds);
        }
    }

    private void initPostCache(Post post) {
        Long postId = post.getId();
        try {
            postCacheProvider.loadCache(postId);
            if (post.getStatus().equals(PostStatus.NORMAL)) {
                postCacheProvider.addToNewestZSet(postId);
                postCacheProvider.addToHotZSet(postId, post.calculateHotScore());
            }
        } catch (Exception e) {
            log.warn("【帖子模块】PostId:{} Redis服务异常，跳过预热，动作：加载帖子缓存", postId, e);
        }
    }

    private void publishPostEvent(Post post) {
        if (post.getStatus().equals(PostStatus.NORMAL)) {
            PostPublishEvent event = new PostPublishEvent();
            event.setPostId(post.getId());
            event.setUserId(post.getCreatorId());
            postMessageSender.sendPostPublish(event);
        }
    }

    /**
     * 构建图片领域实体
     */
    private List<PostImage> buildImages(Long postId, List<ImageInfo> images) {
        if (images == null) {
            return List.of();
        }
        return images.stream()
                .map(img -> PostImage.builder()
                        .postId(postId)
                        .imageUrl(img.getUrl())
                        .width(img.getWidth())
                        .height(img.getHeight())
                        .status(PostStatus.NORMAL)
                        .sortOrder(0)
                        .build())
                .toList();
    }

    @Override
    public void updatePost(UpdatePostCommand command) {
        Post post = postRepository.findById(command.getId());
        validatePostForUpdate(command, post);
        List<ImageInfo> mediaUrls = command.getMediaUrls();
        Long postId = post.getId();
        String mediaUrlsStr = "";
        if (mediaUrls != null) {
            if (PostType.IMAGE.getCode() != command.getType() && PostType.TEXT.getCode() != command.getType()) {
                mediaUrlsStr = mediaUrls.stream()
                        .map(ImageInfo::getUrl)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse("");
            }
        }

        // 局部更新
        post.updatePost(
                Post.builder()
                        .id(command.getId())
                        .title(command.getTitle())
                        .type(PostType.fromCode(command.getType()))
                        .content(textSafeServiceProvider.xssFilter(command.getContent()))
                        .mediaUrls(mediaUrlsStr)
                        .urls(mediaUrls != null ? mediaUrls.stream().map(image ->
                                PostImage.builder()
                                        .imageUrl(image.getUrl())
                                        .postId(postId)
                                        .width(image.getWidth())
                                        .height(image.getHeight())
                                        .build())
                                .toList() : null)
                        .isTop(command.getIsTop())
                        .build()
        );
        postRepository.save(post);
        if (command.getTagIds() != null) {
            List<Long> oldTagIds = postTagRelRepository.findByPostId(postId).stream()
                    .map(com.summit.stp.post.domain.model.PostTag::getTagId).toList();
            postTagRelAppService.clearPostTags(postId);
            List<Long> newTagIds = List.of();
            if (!command.getTagIds().isEmpty()) {
                newTagIds = getTagIdsByUuids(command.getTagIds());
                postTagRelAppService.bindTag(postId, newTagIds);
            }
            List<Long> finalNewTagIds = newTagIds;
            List<Long> removeTagIds = oldTagIds.stream().filter(id -> !finalNewTagIds.contains(id)).toList();
            List<Long> addTagIds = newTagIds.stream().filter(id -> !oldTagIds.contains(id)).toList();
            if (!removeTagIds.isEmpty()) {
                tagCacheProvider.removePostFromTags(postId, removeTagIds);
            }
            if (!addTagIds.isEmpty()) {
                tagCacheProvider.addPostToTags(postId, addTagIds);
            }
        }
        try {
            postCacheProvider.deletePostContent(postId);
            postCacheProvider.cachePostStatus(postId, post.getStatus().getCode());
        } catch (Exception e) {
            log.warn("【帖子模块】更新帖子缓存异常，postId={}", postId, e);
        }
    }



    @Override
    public void deletePost(Long id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new ParameterException("帖子不存在");
        }
        if (!Objects.equals(post.getCreatorId(), UserHolder.getUser().getId())) {
            throw new BusinessException("无权删除他人帖子");
        }
        List<Long> tagIds = postTagRelRepository.findByPostId(id).stream()
                .map(com.summit.stp.post.domain.model.PostTag::getTagId).toList();
        post.delete();
        postRepository.update(post);
        try {
            postCacheProvider.deletePostContent(id);
            postCacheProvider.cachePostStatus(id, PostStatus.DELETED.getCode());
            postCacheProvider.removeFromQueryZSets(id);
            if (!tagIds.isEmpty()) {
                tagCacheProvider.removePostFromTags(id, tagIds);
            }
        } catch (Exception e) {
            log.warn("【帖子模块】更新帖子删除状态缓存异常，postId={}", id, e);
        }
    }

    @Override
    public void republishPost(Long id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new ParameterException("帖子不存在");
        }
        if (!Objects.equals(post.getCreatorId(), UserHolder.getUser().getId())) {
            throw new BusinessException("无权重新发布他人帖子");
        }
        if (post.getStatus() == PostStatus.BLOCKED) {
            throw new BusinessException("该帖子已被封禁，无法重新发布");
        }
        post.republish();
        postRepository.update(post);
        try {
            postCacheProvider.deletePostContent(id);
            postCacheProvider.cachePostStatus(id, PostStatus.NORMAL.getCode());
        } catch (Exception e) {
            log.warn("【帖子模块】更新帖子重新发布状态缓存异常，postId={}", id, e);
        }
    }

    @Override
    public List<PostVO> getPostPage(QueryPostListByCursorCommand command) {
        return postQueryService.getPostPage(
                command.getCursor(),
                command.getSelf(),
                command.getCreatorId(),
                command.getStatus(),
                command.getOrderType(),
                10
        );
    }



    private void toggleInteraction(Long postId, InteractionType type) {
        Long userId = UserHolder.getUser().getId();
        String actionName = type == InteractionType.LIKE ? "点赞" : "收藏";
        try {
            Post post = postRepository.findById(postId);
            if (post == null || !post.isActive()) {
                throw new BusinessException("帖子状态异常,无法" + actionName);
            }
            boolean isOnce = type == InteractionType.LIKE 
                    ? postCacheProvider.like(postId, userId) 
                    : postCacheProvider.collect(postId, userId);
            if (isOnce) {
                applicationEventPublisher.publishEvent(PostInteractionEvent.builder()
                        .postId(postId)
                        .userId(userId)
                        .interactionType(type.name())
                        .timestamp(Instant.now())
                        .build());
            }
        } catch (Exception e) {
            log.warn("【帖子模块】{}帖子异常，postId={}", actionName, postId, e);
            fallbackInteractionInDb(postId, userId, type);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likePost(Long postId) {
        toggleInteraction(postId, InteractionType.LIKE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectPost(Long postId) {
        toggleInteraction(postId, InteractionType.COLLECT);
    }

    @Override
    public boolean isLiked(Long postId) {
        return postCacheProvider.isLiked(postId, UserHolder.getUser().getId());
    }

    @Override
    public boolean isCollected(Long postId) {
        return postCacheProvider.isCollected(postId, UserHolder.getUser().getId());
    }



    /**
     * 数据库降级互动处理（Toggle 模式）
     * 点赞需同步更新帖子 like_count（增量 SQL），收藏无计数维护。
     */
    private void fallbackInteractionInDb(Long postId, Long userId, InteractionType type) {
        boolean isLike = type == InteractionType.LIKE;
        boolean exists = isLike
                ? postLikeRepository.exists(postId, userId)
                : postCollectRepository.exists(postId, userId);

        if (exists) {
            if (isLike) {
                postLikeRepository.delete(postId, userId);
                updateLikeCountDelta(postId, -1);
            } else {
                postCollectRepository.delete(postId, userId);
            }
        } else {
            long id = IdUtil.getSnowflakeNextId();
            if (isLike) {
                postLikeRepository.save(PostLikePO.builder().id(id).postId(postId).userId(userId).build());
                updateLikeCountDelta(postId, 1);
            } else {
                postCollectRepository.save(PostCollectPO.builder().id(id).postId(postId).userId(userId).build());
            }
            applicationEventPublisher.publishEvent(PostInteractionEvent.builder()
                    .postId(postId)
                    .userId(userId)
                    .interactionType(type.name())
                    .timestamp(Instant.now())
                    .build());
        }
    }

    /**
     * 增量更新帖子点赞数，替代 findById + changeSate + save 的全量操作
     */
    private void updateLikeCountDelta(Long postId, long delta) {
        LambdaUpdateWrapper<PostsPO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PostsPO::getId, postId)
                .setSql("like_count = like_count + " + delta);
        postsMapper.update(null, wrapper);
    }
 
    private List<Long> getTagIdsByUuids(List<String> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return List.of();
        }
        return tagRepository.findIdsByUuids(uuids);
    }

    // ======================== 其他操作 ========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void topPost(Long id, Integer isTop) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new ParameterException("帖子不存在");
        }
        if (!Objects.equals(post.getCreatorId(), UserHolder.getUser().getId())) {
            throw new BusinessException("无权置顶他人帖子");
        }
        if (isTop == null || (isTop != 0 && isTop != 1)) {
            throw new ParameterException("置顶参数错误");
        }
        post.top(isTop);
        postRepository.update(post);
    }

    @Override
    public void viewPost(Long id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new ParameterException("帖子不存在");
        }
        postCacheProvider.incrViewCount(id);
    }

    @Override
    public void visibleSelf(Long id, Integer visible) {
        Post post = postRepository.findById(id);
        if (post.getCreatorId().equals(UserHolder.getUser().getId())) {
            post.updateScope(visible);
            postRepository.update(post);
            return;
        }
        throw new BusinessException("无权修改他人帖子");
    }



    /**
     * 帖子内容公共校验：标题长度、内容长度、图片数量上限
     */
    private void validatePostContent(String title, String content, List<ImageInfo> mediaUrls) {
        if (title != null && title.length() > PostConstants.Business.MAX_TITLE_LENGTH) {
            throw new ParameterException("帖子标题长度不能超过" + PostConstants.Business.MAX_TITLE_LENGTH + "字");
        }
        if (content != null && content.length() > PostConstants.Business.MAX_CONTENT_LENGTH) {
            throw new ParameterException("帖子内容长度不能超过" + PostConstants.Business.MAX_CONTENT_LENGTH + "字");
        }
        if (mediaUrls != null && Post.isLimited(mediaUrls.size())) {
            throw new ParameterException("图片数量超过上限!");
        }
    }

    /**
     * 帖子更新前的校验
     */
    private void validatePostForUpdate(UpdatePostCommand command, Post post) {
        if (post == null) {
            throw new ParameterException("帖子不存在");
        }
        if (post.getCreatorId() != command.getCreatorId()) {
            throw new BusinessException("无权修改他人帖子");
        }
        PostStatus status = PostStatus.fromCode(command.getStatus());
        if ((status.equals(PostStatus.BLOCKED) || status.equals(PostStatus.NORMAL)) && UserHolder.getUser().getAdmin() != 1) {
            throw new BusinessException("无权管理帖子的封禁");
        }
        validatePostContent(command.getTitle(), command.getContent(), command.getMediaUrls());
    }

    /**
     * 帖子创建前的校验
     */
    private void validatePostForCreate(CreatePostCommand command) {
        PostStatus status = PostStatus.fromCode(command.getStatus());
        if (status.equals(PostStatus.BLOCKED) || status.equals(PostStatus.NORMAL)) {
            command.setStatus(PostStatus.NORMAL.getCode());
        }
        validatePostContent(command.getTitle(), command.getContent(), command.getMediaUrls());
    }
}

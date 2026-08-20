package com.summit.stp.post.application.service.impl;


import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.application.service.TextSafe.TextSafeServiceProvider;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.application.domain.event.PostChangeEvent;
import com.summit.stp.common.application.domain.event.PostInteractionEvent;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.event.EsPostUpdateEvent;
import com.summit.stp.common.application.domain.event.PostChangeEvent;
import com.summit.stp.elasticsearch.document.PostDocument;
import com.summit.stp.post.api.dto.request.ImageInfo;
import com.summit.stp.post.application.command.CreatePostCommand;
import com.summit.stp.post.application.command.QueryPostListByCursorCommand;
import com.summit.stp.post.application.service.*;
import com.summit.stp.post.application.service.impl.cache.InteractionType;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.domain.exception.NoSuchPostException;
import com.summit.stp.post.domain.model.*;
import com.summit.stp.post.domain.repository.*;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostCollectPO;
import com.summit.stp.post.infrastructure.persistence.po.PostLikePO;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import com.summit.stp.tag.application.service.TagCacheProvider;
import com.summit.stp.tag.domain.model.PostTag;
import com.summit.stp.tag.domain.model.Tag;
import com.summit.stp.tag.domain.repository.PostTagRelRepository;
import com.summit.stp.tag.domain.repository.TagRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;


@Slf4j
@Service
@RequiredArgsConstructor
public class PostAppServiceImpl implements PostAppService {
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final TextSafeServiceProvider textSafeServiceProvider;
    private final PostQueryService postQueryService;
    private final PostCacheProvider postCacheProvider;
    private final PostMessageSender postMessageSender;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final PostsMapper postsMapper;
    private final TagRepository tagRepository;
    private final PostTagRelRepository postTagRelRepository;
    private final TagCacheProvider tagCacheProvider;
    private final TransactionTemplate transactionTemplate;

    @Override
    public PostVO getPostById(Long id) {
        return postQueryService.findById(id, UserHolder.getUser().getId(), PostStatus.NORMAL.getCode());
    }

    @Override
    public Result<Void> createPost(CreatePostCommand command) {
        validatePostForCreate(command);
        AtomicReference<Long> postId = new AtomicReference<>();

        List<Long> actualTagIds = tagRepository.findByIds(command.getTagIds()).stream().map(Tag::getId).toList();
        Post post = transactionTemplate.execute(status -> {
            // 1. 构建并保存帖子主体
            Post p = buildNewPost(command);
            Long newValue = postRepository.savePost(p);
            postId.set(newValue);
            // 2. 保存帖子图片关联
            savePostImages(newValue, p, command.getMediaUrls());
            // 3. 绑定帖子标签
            postTagRelRepository.batchSave(postId.get(), actualTagIds);
            return p;
        });
        // 4. 发布帖子发布事件与系统 ES 更新事件
        try {
            EsPostUpdateEvent esEvent = EsPostUpdateEvent.builder()
                    .postId(postId.get())
                    .eventType(EsPostUpdateEvent.EventType.CREATE)
                    .status(post != null && post.getStatus() != null ? post.getStatus().getCode() : 1)
                    .timestamp(System.currentTimeMillis())
                    .build();
            postMessageSender.sendEsPostUpdateEvent(esEvent);
        } catch (Exception e) {
            log.warn("【帖子模块】发送发帖或系统ES更新事件异常，postId={}", postId, e);
        }

        return Result.success();
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
    public boolean isCollected(Long postId) {
        return postCacheProvider.isCollected(postId, UserHolder.getUser().getId());
    }

    @Override
    public void initPostCache(Post post) {
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

    @Override
    public void publishPostEvent(Post post, PostChangeEvent.EventType eventType, List<Long> tagIds, Long postId) {
        if (post == null) return;
        // 非删除事件且状态不是 NORMAL 时拦截；删除事件放行
        if (eventType == PostChangeEvent.EventType.DELETE) {
            return;
        }
        PostDocument data = PostDocument.builder()
                .id(postId)
                .postId(postId)
                .title(post.getTitle())
                .content(post.getContent())
                .creatorId(post.getCreatorId())
                .createTime(post.getCreateTime())
                .status(post.getStatus().getCode())
                .likeCount(post.getLikeCount())
                .updateTime(post.getUpdateTime())
                .comment(post.getReplyCount())
                .build();

        PostChangeEvent event = PostChangeEvent.builder()
                .postId(post.getId())
                .uid(post.getCreatorId())
                .eventType(eventType)
                .data(data)
                .tagIds(tagIds)
                .build();
        postMessageSender.sendPostChangeEvent(event);
    }

    @Override
    public void deletePost(Long id) {
        if(id == null)throw new BusinessException("参数错误");
        List<Long> tagIds = postTagRelRepository.findByPostId(id)
                .stream()
                .map(PostTag::getTagId).toList();

        // 1, 根据帖子id获取帖子信息
        Post post = postRepository.findById(id).orElseThrow(() -> new ParameterException("帖子不存在"));
        if (!Objects.equals(post.getCreatorId(), UserHolder.getUser().getId())) {
            throw new BusinessException("无权删除他人帖子");
        }
        post.delete();
        //更新帖子
        postRepository.update(post);
        // 删除帖子额外信息
        deletePostExtraInfo(post,tagIds);
        // 发送帖子删除事件
        publishPostEvent(post, PostChangeEvent.EventType.DELETE, tagIds , post.getId());  // 发布帖子删除事件
    }

    @Override
    public void republishPost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ParameterException("帖子不存在"));
        if (!Objects.equals(post.getCreatorId(), UserHolder.getUser().getId())) {
            throw new BusinessException("无权重新发布他人帖子");
        }
        if (post.getStatus() == PostStatus.BLOCKED) {
            throw new BusinessException("该帖子已被封禁，无法重新发布");
        }
        post.republish();
        postRepository.update(post);
        try {
            publishPostEvent(post, PostChangeEvent.EventType.CREATE, null, post.getId());
            postCacheProvider.deletePostContent(id);
            postCacheProvider.cachePostStatus(id, PostStatus.NORMAL.getCode());
        } catch (Exception e) {
            log.warn("【帖子模块】更新帖子重新发布事件及缓存异常，postId={}", id, e);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void topPost(Long id, Integer isTop) {
        Post post = postRepository.findById(id).orElseThrow(() -> new ParameterException("帖子不存在"));
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
        postRepository.findById(id).orElseThrow(() -> new ParameterException("帖子不存在"));
        postCacheProvider.incrViewCount(id);
    }

    @Override
    public void visibleSelf(Long id, Integer visible) {
        Post post = postRepository.findById(id).orElseThrow(NoSuchPostException::new);
        if (!post.getCreatorId().equals(UserHolder.getUser().getId())) throw new BusinessException("无权修改他人帖子");
        post.updateScope(visible);
        postRepository.update(post);

    }
    @Override
    public String getUnpassReason(Long id) {
        Post post = postRepository.findById(id).orElseThrow(NoSuchPostException::new);
        if (!post.getCreatorId().equals(UserHolder.getUser().getId())) {
            throw new BusinessException("无权限查看");
        }
        return post.getUnpassReason();
    }
    /**
     * 构建帖子实体
     *
     * @param command 创建帖子命令
     * @return 帖子实体
     */
    private Post buildNewPost(CreatePostCommand command) {
        List<ImageInfo> mediaUrls = command.getMediaUrls();
        String mediaUrlsStr = resolvePostMediaMetaInfo(mediaUrls, command.getType());
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Long userId = UserHolder.getUser().getId();
        return Post.builder()
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
                .status(PostStatus.CHECK)
                .createTime(now)
                .updateTime(now)
                .isTop(command.getIsTop() == null ? 0 : command.getIsTop())
                .build();
    }


    /**
     * 解析帖子媒体元信息
     *
     * @param mediaUrls 媒体链接
     * @param type      发布类型
     * @return 非图片类型信息的url
     */
    private String resolvePostMediaMetaInfo(List<ImageInfo> mediaUrls, Integer type) {
        String mediaUrlsStr = "";
        if (mediaUrls != null) {
            if (PostType.IMAGE.getCode() != type && PostType.TEXT.getCode() != type) {
                mediaUrlsStr = mediaUrls.stream()
                        .map(ImageInfo::getUrl)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse("");
            }
        }
        return mediaUrlsStr;
    }

    /**
     * 批量保存帖子图片
     *
     * @param postId    生成的帖子ID
     * @param post      帖子实体
     * @param mediaUrls 图片链接集
     */
    private void savePostImages(Long postId, Post post, List<ImageInfo> mediaUrls) {
        if (post.isImage() && mediaUrls != null && !mediaUrls.isEmpty()) {
            List<PostImage> urls = buildImages(postId, mediaUrls);
            postImageRepository.batchSave(urls);
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


    /**
     * 删除除了本体以外的所有信息
     *
     * @param post 帖子实体
     */
    @Override
    public void deletePostExtraInfo(Post post, @Nullable List<Long> tagIds) {
        Long id = post.getId();
         if(tagIds == null) {
             tagIds = postTagRelRepository.findByPostId(id)
                     .stream()
                     .map(PostTag::getTagId).toList();
         }
        try {
            postCacheProvider.deletePostContent(id);            // 删除帖子内容缓存
            postCacheProvider.cachePostStatus(id, PostStatus.DELETED.getCode());            // 缓存帖子状态
            postCacheProvider.removeFromQueryZSets(id);            // 从查询缓存中移除
            if (!tagIds.isEmpty()) {
                tagCacheProvider.removePostFromTags(id, tagIds);
            }
        } catch (Exception e) {
            log.warn("【帖子模块】更新帖子删除状态缓存异常，postId={}", id, e);
        }
    }


    /**
     * 帖子互动操作执行
     *
     * @param postId 帖子ID
     * @param type   互动类型
     */
    private void toggleInteraction(Long postId, InteractionType type) {
        Long userId = UserHolder.getUser().getId();
        String actionName = type == InteractionType.LIKE ? "点赞" : "收藏";
        try {
            Post post = postRepository.findById(postId).orElse(null);
            if (post == null || !post.isActive()) {
                throw new BusinessException("帖子状态异常,无法" + actionName);
            }
            boolean isOnce = type == InteractionType.LIKE
                    ? postCacheProvider.like(postId, userId)
                    : postCacheProvider.collect(postId, userId);

            postMessageSender.sendPostInteraction(PostInteractionEvent.builder()
                    .isOnce(isOnce)
                    .postId(postId)
                    .userId(userId)
                    .interactionType(type.name())
                    .timestamp(Instant.now())
                    .build());
        } catch (Exception e) {
            log.warn("【帖子模块】{}帖子异常，postId={}", actionName, postId, e);
            fallbackInteractionInDb(postId, userId, type);
        }
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
            postMessageSender.sendPostInteraction(PostInteractionEvent.builder()
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


    // ======================== 其他操作 ========================


    /**
     * 帖子内容公共校验：标题长度、内容长度、图片数量上限
     */
    private void validatePostContent(String title, String content, List<ImageInfo> mediaUrls) {
        if (title != null && title.length() > Post.MAX_TITLE_LENGTH) {
            throw new ParameterException("帖子标题长度不能超过" + Post.MAX_TITLE_LENGTH + "字");
        }
        if (content != null && content.length() > Post.MAX_CONTENT_LENGTH) {
            throw new ParameterException("帖子内容长度不能超过" + Post.MAX_CONTENT_LENGTH + "字");
        }
        if (mediaUrls != null && Post.isLimited(mediaUrls.size())) {
            throw new ParameterException("图片数量超过上限!");
        }
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




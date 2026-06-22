package com.summit.stp.post.application.service.impl;


import cn.hutool.core.util.IdUtil;
import com.summit.stp.post.api.dto.request.ImageInfo;
import com.summit.stp.post.application.command.CreatePostCommand;
import com.summit.stp.post.application.command.QueryPostListByCursorCommand;
import com.summit.stp.post.application.command.UpdatePostCommand;
import com.summit.stp.post.application.service.*;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.domain.event.PostPublishEvent;
import com.summit.stp.post.domain.exception.NoSuchPostException;
import com.summit.stp.post.domain.model.*;
import com.summit.stp.post.domain.repository.PostImageRepository;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.domain.repository.PostTagRelRepository;
import com.summit.stp.post.domain.repository.TagRepository;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.exception.BusinessException;
import com.summit.stp.shared.exception.ParameterException;
import com.summit.stp.shared.result.Result;
import com.summit.stp.shared.service.TextSafe.TextSafeServiceProvider;
import com.summit.stp.user.application.UserApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import com.summit.stp.shared.constants.BusinessRuleConstants;


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

    @Override
    public PostVO getPostById(Long id) {
        return postQueryService.findById(id, UserHolder.getUser().getId(), PostStatus.NORMAL.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createPost(CreatePostCommand command) {
        PostStatus status = PostStatus.fromCode(command.getStatus());
        validatePostForCreate(command);
        List<ImageInfo> mediaUrls = command.getMediaUrls();
        String mediaUrlsStr = "";
        if (mediaUrls != null) {
            if (PostType.IMAGE.getCode() != command.getType() && PostType.TEXT.getCode() != command.getType()) {
                // 非图片、非纯文本的帖子如带视频的帖子放在主表的mediaUrls字段，且无需逗号分隔
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
        Post post = Post.builder()
                .id(postId)
                .creatorId(userId)
                .title(command.getTitle())
                .type(PostType.fromCode(command.getType()))
                .content(textSafeServiceProvider.xssFilter(command.getContent()))
                .mediaUrls(mediaUrlsStr)
                .status(status)
                .createTime(now)
                .updateTime(now)
                .isTop(command.getIsTop() == null ? 0 : command.getIsTop())
                .build();

        post = postRepository.save(post);


        //保存帖子包含的图片信息
        postId = post.getId();

        if (post.isImage() && mediaUrls != null && !mediaUrls.isEmpty()) {
            List<PostImage> urls = buildImages(postId, mediaUrls);
            postImageRepository.batchSave(urls);
        }

        if (command.getTagIds() != null && !command.getTagIds().isEmpty()) {
            postTagRelAppService.bindTag(postId, command.getTagIds());
        }

        //加载缓存(点赞,收藏)
        postCacheProvider.loadCache(postId);

        if (post.getStatus().equals(PostStatus.NORMAL)) {
            //发布帖子发布事件
            PostPublishEvent event = new PostPublishEvent();
            event.setPostId(postId);
            event.setUserId(userId);
            postMessageSender.sendPostPublish(event);
        }
        return Result.success();
    }


    /**
     * 构建图片领域实体
     */
    private List<PostImage> buildImages(Long postId, List<ImageInfo> images) {
        if (images == null) {
            return List.of();
        }
        return images.stream()
                .map(img -> {
                    return PostImage.builder()
                            .postId(postId)
                            .imageUrl(img.getUrl())
                            .width(img.getWidth())
                            .height(img.getHeight())
                            .status(PostStatus.NORMAL)
                            .sortOrder(0)
                            .build();
                })
                .toList();
    }

    @Override
    public void updatePost(UpdatePostCommand command) {
        Post post = postRepository.findById(command.getId());
        validatePostForUpdate(command,post);
        List<ImageInfo> mediaUrls = command.getMediaUrls();
        Long postId = post.getId();
        String mediaUrlsStr = "";
        if (mediaUrls != null) {
            if (PostType.IMAGE.getCode() != command.getType() && PostType.TEXT.getCode() != command.getType()) {
                // 非图片、非纯文本的帖子如带视频的帖子放在主表的mediaUrls字段
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
                        .urls(mediaUrls.stream().map(image ->
                                PostImage.builder()
                                        .imageUrl(image.getUrl())
                                        .postId(postId)
                                        .width(image.getWidth())
                                        .height(image.getHeight())
                                        .build())
                                .toList())
                        .isTop(command.getIsTop())
                        .build()
        );
        postRepository.save(post);
        if (command.getTagIds() != null) {
            postTagRelAppService.clearPostTags(postId);
            if (!command.getTagIds().isEmpty()) {
                postTagRelAppService.bindTag(postId, command.getTagIds());
            }
        }
    }

    /**
     * 判断帖子是否可操作
     *
     * @param postId 帖子id
     * @return
     */
    public boolean postIsActive(Long postId) {
        //先看帖子有没有被删除,如果被删除了,不能获取图片
        PostVO postVO = getPostById(postId);
        if (postVO == null) {
            throw new NoSuchPostException("未能找到帖子信息");
        }
        return postVO.getStatus().equals(PostStatus.NORMAL.getCode());

    }

    @Override
    public void deletePost(Long id) {
        //帖子的发布者是否为本人
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new ParameterException("帖子不存在");
        }
        if (post.getCreatorId() != UserHolder.getUser().getId()) {
            throw new BusinessException("无权删除他人帖子");
        }
        post.delete();

        postRepository.update(post);
    }

    @Override
    public void republishPost(Long id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            throw new ParameterException("帖子不存在");
        }
        if (post.getCreatorId() != UserHolder.getUser().getId()) {
            throw new BusinessException("无权重新发布他人帖子");
        }
        if (post.getStatus() == PostStatus.BLOCKED) {
            throw new BusinessException("该帖子已被封禁，无法重新发布");
        }
        post.republish();

        postRepository.update(post);
    }

    @Override
    public List<PostVO> getPostPage(QueryPostListByCursorCommand command) {
        return postQueryService.getPostPage(
                command.getCursor(),
                command.getSelf(),
                command.getCreatorId(),
                command.getStatus(),
                10
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likePost(Long postId) {
        Long userId = UserHolder.getUser().getId();
        //1,查看帖子状态
        if (postIsActive(postId)) {
            //2,点赞
            postCacheProvider.like(postId, userId);
        } else {
            throw new BusinessException("帖子状态异常,无法点赞");
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectPost(Long postId) {
        Long userId = UserHolder.getUser().getId();
        if (postIsActive(postId)) {
            //2,收藏
            postCacheProvider.collect(postId, userId);
        } else {
            throw new BusinessException("帖子状态异常,无法收藏");
        }
    }

    @Override
    public boolean isLiked(Long postId) {
        return postCacheProvider.isLiked(postId, UserHolder.getUser().getId());
    }

    @Override
    public boolean isCollected(Long postId) {
        return postCacheProvider.isCollected(postId, UserHolder.getUser().getId());
    }

    @Override
    public List<PostVO> getMyCollectPostList(Long targetUserId, String cursor) {
        return postQueryService.getMyCollectPostList(targetUserId, cursor);
    }

    @Override
    public List<PostVO> getMyLikePostList(Long targetUserId, String cursor) {
        return postQueryService.getMyLikePostList(targetUserId, cursor);
    }



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
        if(post.getCreatorId().equals(UserHolder.getUser().getId())) {
            post.updateScope(visible);
            postRepository.update(post);
            return;
        }
        throw new BusinessException("无权修改他人帖子");
    }

    /**
     * 帖子更新前的校验
     *
     * @param command 帖子更新参数
     * @param post 帖子
     */
    private void validatePostForUpdate(UpdatePostCommand command,Post post){
        if (post == null) {
            throw new ParameterException("帖子不存在");
        }
        if (post.getCreatorId() != command.getCreatorId()) {
            throw new BusinessException("无权修改他人帖子");
        }
        PostStatus status = PostStatus.fromCode(command.getStatus());
        if ((status.equals(PostStatus.BLOCKED) || status.equals(PostStatus.NORMAL) && UserHolder.getUser().getAdmin() != 1)) {
            //用户不能管理封禁的帖子
            throw new BusinessException("无权管理帖子的封禁");
        }
        List<ImageInfo> mediaUrls = command.getMediaUrls();
        if (command.getTitle() != null && command.getTitle().length() > BusinessRuleConstants.Post.MAX_TITLE_LENGTH) {
            throw new ParameterException("帖子标题长度不能超过" + BusinessRuleConstants.Post.MAX_TITLE_LENGTH + "字");
        }
        if (command.getContent() != null && command.getContent().length() > BusinessRuleConstants.Post.MAX_CONTENT_LENGTH) {
            throw new ParameterException("帖子内容长度不能超过" + BusinessRuleConstants.Post.MAX_CONTENT_LENGTH + "字");
        }
        if (mediaUrls != null && Post.isLimited(mediaUrls.size())) {
            throw new ParameterException("图片数量超过上限!");
        }
    }
    /**
     * 帖子创建前的校验
     *
     * @param command 创建帖子命令
     */
    private void validatePostForCreate(CreatePostCommand command){
        PostStatus status = PostStatus.fromCode(command.getStatus());
        if ((status.equals(PostStatus.BLOCKED) || status.equals(PostStatus.NORMAL))) {
            //用户不能管理封禁的帖子
            command.setStatus(PostStatus.NORMAL.getCode());  // 默认为正常
        }
        if (command.getTitle() != null && command.getTitle().length() > BusinessRuleConstants.Post.MAX_TITLE_LENGTH) {
            throw new ParameterException("帖子标题长度不能超过" + BusinessRuleConstants.Post.MAX_TITLE_LENGTH + "字!");
        }
        if (command.getContent() != null && command.getContent().length() > BusinessRuleConstants.Post.MAX_CONTENT_LENGTH) {
            throw new ParameterException("帖子内容长度不能超过" + BusinessRuleConstants.Post.MAX_CONTENT_LENGTH + "字!");
        }
        //图片数量是否到达上限
        List<ImageInfo> mediaUrls = command.getMediaUrls();
        if (mediaUrls != null && Post.isLimited(mediaUrls.size())) {
            throw new ParameterException("图片数量超过上限!");
        }
            /*    //敏感词过滤
        if (textSafeServiceProvider.sensitiveDetect(post.getContent()) || textSafeServiceProvider.sensitiveDetect(post.getTitle())) {
              post.draft();  //出现敏感词,阻止帖子发布
                postRepository.save(post);
                return Result.error("内容包含敏感词,已存入草稿,请检查后再发布!");
        }*/
    }
}

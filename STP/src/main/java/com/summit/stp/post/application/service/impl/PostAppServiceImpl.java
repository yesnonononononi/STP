package com.summit.stp.post.application.service.impl;


import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.pagehelper.Page;
import com.summit.stp.post.application.command.QueryPostListByCursorCommand;
import com.summit.stp.post.application.service.*;
import com.summit.stp.post.application.vo.PostTagRelVO;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.application.vo.TagVO;
import com.summit.stp.post.domain.repository.PostTagRelRepository;
import com.summit.stp.post.domain.repository.TagRepository;
import com.summit.stp.post.infrastructure.persistence.po.TagPO;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.model.PostImage;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.domain.model.PostType;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import com.summit.stp.post.application.command.CreatePostCommand;
import com.summit.stp.post.application.command.UpdatePostCommand;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.exception.BusinessException;
import com.summit.stp.shared.exception.ParameterException;

import java.sql.Timestamp;

import com.summit.stp.shared.result.Result;
import com.summit.stp.shared.service.TextSafe.TextSafeServiceProvider;
import com.summit.stp.user.application.UserApplicationService;
import com.summit.stp.user.application.vo.UserSimpleVO;
import com.summit.stp.post.domain.repository.PostImageRepository;
import com.summit.stp.post.application.vo.PostImageVO;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.post.domain.repository.PostCollectRepository;
import com.summit.stp.post.infrastructure.persistence.po.PostLikePO;
import com.summit.stp.post.infrastructure.persistence.po.PostCollectPO;
import com.summit.stp.post.api.dto.request.ImageInfo;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PostAppServiceImpl implements PostAppService {
    private final PostRepository postRepository;
    private final UserApplicationService userApplicationService;
    private final PostImageRepository postImageRepository;
    private final TextSafeServiceProvider textSafeServiceProvider;
    private final PostQueryService postQueryService;
    private final PostTagRelAppService postTagRelAppService;
    private final PostTagRelRepository postTagRelRepository;
    private final TagRepository tagRepository;
    private final PostCacheProvider postCacheProvider;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;

    @Override
    public PostVO getPostById(Long id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            return null;
        }

        postCacheProvider.loadCache(id);
        return convertToVO(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createPost(CreatePostCommand command) {
        PostStatus status = PostStatus.fromCode(command.getStatus());
        if ((status.equals(PostStatus.BLOCKED) || status.equals(PostStatus.NORMAL))) {
            //用户不能管理封禁的帖子
            status = PostStatus.NORMAL;  // 默认为正常
        }

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
        //图片数量是否到达上限
        if(mediaUrls != null && Post.isLimited(mediaUrls.size())){
            return Result.error("图片数量超过上限!");
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        Post post = Post.builder()
                .creatorId(UserHolder.getUser().getId())
                .title(command.getTitle())
                .type(PostType.fromCode(command.getType()))
                .content(command.getContent())
                .mediaUrls(mediaUrlsStr)
                .status(status)
                .createTime(now)
                .updateTime(now)
                .build();
    /*    //敏感词过滤
        if (textSafeServiceProvider.sensitiveDetect(post.getContent()) || textSafeServiceProvider.sensitiveDetect(post.getTitle())) {
              post.draft();  //出现敏感词,阻止帖子发布
                postRepository.save(post);
                return Result.error("内容包含敏感词,已存入草稿,请检查后再发布!");
        }*/
        post = postRepository.save(post);


        //保存帖子包含的图片信息
        Long postId = post.getId();

        if(post.isImage() && mediaUrls != null && !mediaUrls.isEmpty()){
            List<PostImage> urls = buildImages(postId, mediaUrls);
            postImageRepository.batchSave(urls);
        }

        if (command.getTagIds() != null && !command.getTagIds().isEmpty()) {
            postTagRelAppService.bindTag(postId, command.getTagIds());
        }

        //加载缓存(点赞,收藏)
        postCacheProvider.loadCache(postId);

        if(post.getStatus().equals(PostStatus.NORMAL)){
            //发布帖子发布事件
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

        // 局部更新
        post.updatePost(
                Post.builder()
                        .id(command.getId())
                        .title(command.getTitle())
                        .type(PostType.fromCode(command.getType()))
                        .content(command.getContent())
                        .mediaUrls(mediaUrlsStr)
                        .build()
        );
        postRepository.save(post);
        if (command.getTagIds() != null) {
            postTagRelAppService.clearPostTags(post.getId());
            if (!command.getTagIds().isEmpty()) {
                postTagRelAppService.bindTag(post.getId(), command.getTagIds());
            }
        }
    }
    /**
     * 判断帖子是否可操作
     * @param postId 帖子id
     * @return
     */
    public boolean postIsActive(Long postId){
        //先看帖子有没有被删除,如果被删除了,不能获取图片
        PostVO postVO = getPostById(postId);
        return postVO.getStatus().equals(PostStatus.NORMAL.getCode()) ;

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
                command.getStatus()
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
        }else{
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
        }else{
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


    private PostVO convertToVO(Post post) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setCreatorId(post.getCreatorId());
        vo.setTitle(post.getTitle());
        vo.setType(post.getType().getCode());
        vo.setContent(post.getContent());


        List<PostImageVO> images = postImageRepository.findByPostId(post.getId());
        vo.setMediaUrls(images);
        Long likeCount = postCacheProvider.getLikeCount(post.getId());


        vo.setLikeCount(likeCount != null ? likeCount : 0L);

        vo.setReplyCount(post.getReplyCount() != null ? post.getReplyCount().longValue() : 0L);
        Long collectCount = postCacheProvider.getCollectCount(post.getId());
        vo.setCollectCount(collectCount != null ? collectCount : 0L);
        
        // 填充当前用户的点赞、收藏状态
        UserHolder.getUser();
        vo.setIsLike(postCacheProvider.isLiked(post.getId(), UserHolder.getUser().getId()));
        vo.setIsCollect(postCacheProvider.isCollected(post.getId(), UserHolder.getUser().getId()));

        vo.setStatus(post.getStatus().getCode());
        vo.setCreateTime(post.getCreateTime());
        vo.setUpdateTime(post.getUpdateTime());
        vo.setExtraMediaUrl(post.getMediaUrls());

        List<PostTagRelVO> tagRels = postTagRelRepository.findByPostId(post.getId());
        if (tagRels != null && !tagRels.isEmpty()) {
            List<Long> tagIds = tagRels.stream().map(PostTagRelVO::getTagId).toList();
            List<TagPO> tagPOs = tagRepository.findByIds(tagIds);
            List<TagVO> tagVOs = tagPOs.stream().map(po -> TagVO.builder()
                    .id(po.getId())
                    .tagName(po.getTagName())
                    .sort(po.getSort())
                    .useCount(po.getUseCount())
                    .status(po.getStatus())
                    .createTime(po.getCreateTime())
                    .build()
            ).toList();
            vo.setTags(tagVOs);
        }

        // 核心聚合发布者信息
        UserSimpleVO simpleUser = userApplicationService.findSimpleUserById(post.getCreatorId());
        vo.setPublisher(simpleUser);

        return vo;
    }
}

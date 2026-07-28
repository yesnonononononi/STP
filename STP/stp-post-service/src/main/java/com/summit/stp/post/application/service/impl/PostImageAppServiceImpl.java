package com.summit.stp.post.application.service.impl;

import com.summit.stp.post.application.command.CreatePostImageCommand;
import com.summit.stp.post.application.command.UpdatePostImageCommand;
import com.summit.stp.post.application.service.PostImageAppService;
import com.summit.stp.post.application.vo.PostImageVO;
import com.summit.stp.post.domain.exception.NoSuchPostException;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.model.PostImage;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.domain.repository.PostImageRepository;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.infrastructure.persistence.po.PostImagePO;
import com.summit.stp.common.application.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import org.springframework.context.ApplicationEventPublisher;
import com.summit.stp.common.application.domain.event.FileDeleteEvent;

@Service
@RequiredArgsConstructor
public class PostImageAppServiceImpl implements PostImageAppService {
    private final PostImageRepository postImageRepository;
    private final PostRepository postRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public PostImageVO getPostImageById(Long id) {
        PostImageVO byId = postImageRepository.findById(id);
        if (byId != null) {
            checkPostActive(byId.getPostId());
        }
        return byId;
    }

    @Override
    public List<PostImageVO> getImagesByPostId(Long postId) {
        checkPostActive(postId);
        return postImageRepository.findByPostId(postId);
    }

    public PostImage convertToDomain(PostImagePO po) {
        return PostImage.builder()
                .id(po.getId())
                .postId(po.getPostId())
                .imageUrl(po.getImageUrl())
                .width(po.getWidth())
                .height(po.getHeight())
                .size(po.getSize())
                .sortOrder(po.getSortOrder())
                .status(po.getStatus() == null ? null : PostStatus.fromCode(po.getStatus()))
                .createTime(po.getCreateTime())
                .build();
    }

    /**
     * 增加帖子图片
     */
    @Override
    public void createPostImage(CreatePostImageCommand command) {
        Long postId = command.getPostId();
        List<String> imageUrls = command.getImageUrl();
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        checkPostActive(postId);
        Long curImageCount = postImageRepository.countByPostId(postId);
        if (Post.isLimited(curImageCount + imageUrls.size())) {
            throw new BusinessException("图片数量已到达上限 " + PostConstants.Business.MAX_IMAGE_NUM + "张!");
        }
        List<PostImage> postImages = imageUrls.stream().map(url -> 
            PostImage.builder()
                    .imageUrl(url)
                    .postId(postId)
                    .width(command.getWidth())
                    .height(command.getHeight())
                    .size(command.getSize())
                    .sortOrder(command.getSortOrder() != null ? command.getSortOrder() : 0)
                    .status(command.getStatus() == null ? PostStatus.NORMAL : PostStatus.fromCode(command.getStatus()))
                    .createTime(new Timestamp(System.currentTimeMillis()))
                    .build()
        ).toList();
        postImageRepository.batchSave(postImages);
    }

    @Override
    public void updatePostImage(UpdatePostImageCommand command) {
        PostImageVO byId = postImageRepository.findById(command.getId());
        if (byId == null) throw new NoSuchPostException(command.getId());
        PostImage postImage = convertToDomain(byId);
        postImage.updateImage(command.getImageUrl(), command.getWidth(), command.getHeight(), command.getSize());
        postImage.updateSortOrder(command.getSortOrder());
        postImageRepository.save(postImage);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void deletePostImage(Long id) {
       PostImageVO imageVO = postImageRepository.findById(id);
       postImageRepository.delete(id);
       if (imageVO != null && imageVO.getImageUrl() != null) {
           applicationEventPublisher.publishEvent(new FileDeleteEvent(this, java.util.List.of(imageVO.getImageUrl())));
       }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void deleteImagesByPostId(Long postId) {
       List<PostImageVO> images = postImageRepository.findByPostId(postId);
       postImageRepository.deleteByPostId(postId);
       if (images != null && !images.isEmpty()) {
           List<String> urls = images.stream()
                   .map(PostImageVO::getImageUrl)
                   .filter(java.util.Objects::nonNull)
                   .toList();
           if (!urls.isEmpty()) {
               applicationEventPublisher.publishEvent(new FileDeleteEvent(this, urls));
           }
       }
    }

    private PostImage convertToDomain(PostImageVO vo) {
        return PostImage.builder()
                .id(vo.getId())
                .postId(vo.getPostId())
                .imageUrl(vo.getImageUrl())
                .width(vo.getWidth())
                .height(vo.getHeight())
                .size(vo.getSize())
                .sortOrder(vo.getSortOrder())
                .status(vo.getStatus() == null ? null : PostStatus.fromCode(vo.getStatus()))
                .createTime(vo.getCreateTime())
                .build();
    }

    private void checkPostActive(Long postId) {
        Post post = postRepository.findById(postId);
        if (post == null || post.getStatus() != PostStatus.NORMAL) {
            throw new BusinessException("帖子已被删除!");
        }
    }

}

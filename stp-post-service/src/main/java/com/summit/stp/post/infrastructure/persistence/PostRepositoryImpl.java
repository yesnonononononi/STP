package com.summit.stp.post.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.model.PostImage;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.domain.model.PostType;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.post.infrastructure.persistence.mapper.PostImageMapper;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostImagePO;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {
    private final PostsMapper postsMapper;
    private final PostImageMapper postImageMapper;
    private final PostTagRelRepositoryImpl postTagRelRepositoryImpl;


    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public Post save(Post post) {
        PostsPO po = convertToPo(post);
        Long postId = post.getId();
        boolean isInsert = postId == null || postId == 0 || postsMapper.selectById(postId) == null;
        if (isInsert) {
            postsMapper.insert(po);
            return Post.builder()
                    .id(po.getId())
                    .creatorId(post.getCreatorId())
                    .title(post.getTitle())
                    .type(post.getType())
                    .content(post.getContent())
                    .mediaUrls(post.getMediaUrls())
                    .status(post.getStatus())
                    .createTime(po.getCreateTime())
                    .updateTime(po.getUpdateTime())
                    .isTop(po.getIsTop())
                    .visibleScope(post.getVisibleScope())
                    .viewCount(po.getViewCount())
                    .build();
        } else {
            postsMapper.updateById(po);
        }
        if(PostType.IMAGE.getCode() == post.getType().getCode()){
            List<PostImage> mediaUrls = post.getUrls();
            //图片类型的帖子,先看图片数量是否到达上限
            if(Post.isLimited(mediaUrls.size()))throw new BusinessException("图片数量已到达上限!");

            //找到帖子现有的图片
            List<PostImagePO> dbImages = postImageMapper.selectList(new LambdaQueryWrapper<PostImagePO>().eq(PostImagePO::getPostId, postId));

            //请求的实体映射
            Map<String, PostImage> map = mediaUrls.stream().collect(Collectors.toMap(PostImage::getImageUrl, v -> v));

            //db的图片实体映射
            Map<String, PostImagePO> dbMap = dbImages.stream().collect(Collectors.toMap(PostImagePO::getImageUrl, v -> v));

            List<String> urls = dbImages.stream().map(PostImagePO::getImageUrl).toList();

            List<String> newUrls = mediaUrls.stream().map(PostImage::getImageUrl).toList();
            //新增
            List<PostImagePO> addList = newUrls.stream()
                    .filter(url -> !urls.contains(url))
                    .distinct()
                    .map(map::get)
                    .map(image->PostImagePO.builder()
                            .postId(postId)
                            .width(image.getWidth())
                            .height(image.getHeight())
                            .sortOrder(0)
                            .imageUrl(image.getImageUrl())
                            .size(image.getSize())
                            .createTime(image.getCreateTime())
                            .build())
                    .toList();

            //删除
            List<Long> delList = urls.stream()
                    .filter(url -> !newUrls.contains(url))
                    .map(dbMap::get)
                    .map(PostImagePO::getId)
                    .toList();
            //修改
            postImageMapper.deleteByIds(delList);
            postImageMapper.insert(addList);
        }
        return post;
    }

    @Override
    public void update(Post post) {
      postsMapper.updateById(convertToPo(post));
    }

    @Override
    public List<PostVO> queryByPostIds(List<Long> posts,Integer status,Long userId) {
        return postsMapper.queryByPostIds(posts,status,userId);
    }

    @Override
    public Post findById(Long id) {
        PostsPO po = postsMapper.selectById(id);
        return po == null ? null : convertToDomain(po);
    }

    @Override
    public List<Post> queryMostHotPost(int limit) {
        LambdaQueryWrapper<PostsPO> wrapper = new LambdaQueryWrapper<PostsPO>().eq(PostsPO::getStatus, PostStatus.NORMAL.getCode())
                .orderByDesc(PostsPO::getHotScore)
                .last("limit " + limit);
        return postsMapper.selectList(wrapper).stream().map(this::convertToDomain).toList();

    }

    @Override
    public List<Post> queryPostsByDay(LocalDateTime sevenDaysAgo) {
        LambdaQueryWrapper<PostsPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostsPO::getStatus, PostStatus.NORMAL.getCode())
                .gt(PostsPO::getCreateTime, Timestamp.valueOf(sevenDaysAgo))
                .last("limit " + PostConstants.Business.MAX_POST_SCORE_UPDATE_ONCE);
        return postsMapper.selectList(queryWrapper).stream().map(this::convertToDomain).toList();
    }

    @Override
    public void updateBatchById(List<Post> list) {
        postsMapper.updateById(list.stream().map(this::convertToPo).toList());
    }

    public Post convertToDomain(PostsPO po) {
        return Post.builder()
                .id(po.getId())
                .creatorId(po.getCreatorId())
                .title(po.getTitle())
                .type(PostType.fromCode(po.getType()))
                .content(po.getContent())
                .mediaUrls(po.getMediaUrls())
                .status(PostStatus.fromCode(po.getStatus()))
                .createTime(po.getCreateTime())
                .likeCount(po.getLikeCount())
                .visibleScope(Post.VisibleScope.fromCode(po.getVisibleScope()))
                .updateTime(po.getUpdateTime())
                .isTop(po.getIsTop())
                .viewCount(po.getViewCount())
                .replyCount(po.getReplyCount() != null ? po.getReplyCount().longValue() : 0L)
                .hotScore(po.getHotScore() != null ? po.getHotScore().doubleValue() : 0.0)
                .build();
    }
    public PostsPO convertToPo(Post post) {
        return PostsPO.builder()
                .id(post.getId())
                .creatorId(post.getCreatorId() == null ? 0L : post.getCreatorId())
                .title(post.getTitle())
                .type(post.getType() == null ? 0 : post.getType().getCode())
                .content(post.getContent())
                .mediaUrls(post.getMediaUrls())
                .status(post.getStatus() == null ? 0 : post.getStatus().getCode())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .isTop(post.getIsTop())
                .viewCount(post.getViewCount())
                .visibleScope(post.getVisibleScope().getCode())
                .likeCount(post.getLikeCount())
                .collectCount(post.getCollectCount())
                .replyCount(post.getReplyCount() != null ? post.getReplyCount().intValue() : 0)
                .hotScore(post.getHotScore() != null ? post.getHotScore().longValue() : 0L)
                .build();
    }

    @Override
    public List<PostVO> queryByPage(Long cursor, Long creatorId, Long userId, Integer status, Integer limit) {
        return postsMapper.queryByPage(cursor, creatorId, userId, status, limit);
    }

    @Override
    public List<Long> getPostsByTag(Long tagId, String cursor, Integer limit) {
        return postsMapper.getPostsByTag(tagId, cursor, limit);
    }

    public List<Long> getHotPostsByTag(Long tagId, String cursor, Integer limit) {
       return postsMapper.getHotPostsByTag(tagId, cursor, limit);
    }
}

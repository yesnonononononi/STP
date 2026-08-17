package com.summit.stp.post.infrastructure.persistence.repoImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
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
import com.summit.stp.tag.infrastructure.persistence.PostTagRelRepositoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PostRepositoryImpl extends AbstractRepository<Post, PostsPO> implements PostRepository {
    private final PostsMapper postsMapper;
    private final PostImageMapper postImageMapper;


    public PostRepositoryImpl(PostsMapper postsMapper, PostImageMapper postImageMapper ) {
        super(postsMapper);
        this.postsMapper = postsMapper;
        this.postImageMapper = postImageMapper;

    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void save(Post post) {
        if (post == null) return;
        if (post.getId() != null && findById(post.getId()).isPresent()) {
            super.updateById(post);
        } else {
            super.save(post);
        }
        Long postId = post.getId();
        if (post.getType() != null && PostType.IMAGE.getCode() == post.getType().getCode() && postId != null) {
            List<PostImage> mediaUrls = post.getUrls();
            if (Post.isLimited(mediaUrls.size())) throw new BusinessException("图片数量已到达上限!");

            List<PostImagePO> dbImages = postImageMapper.selectList(new LambdaQueryWrapper<PostImagePO>().eq(PostImagePO::getPostId, postId));
            Map<String, PostImage> map = mediaUrls.stream().collect(Collectors.toMap(PostImage::getImageUrl, v -> v));
            Map<String, PostImagePO> dbMap = dbImages.stream().collect(Collectors.toMap(PostImagePO::getImageUrl, v -> v));

            List<PostImagePO> addList = new ArrayList<>();
            List<Long> delList = new ArrayList<>();
            map.forEach((k, v) -> {
                if (!dbMap.containsKey(k)) {
                    addList.add(PostImagePO.builder()
                            .postId(postId)
                            .imageUrl(v.getImageUrl())
                            .width(v.getWidth())
                            .height(v.getHeight())
                            .size(v.getSize())
                            .sortOrder(v.getSortOrder())
                            .status(v.getStatus().getCode())
                            .createTime(Timestamp.from(Instant.now()))
                            .build());
                }
            });

            dbMap.forEach((k, v) -> {
                if (!map.containsKey(k)) {
                    delList.add(v.getId());
                }
            });

            if (!delList.isEmpty()) {
                postImageMapper.deleteByIds(delList);
            }
            if (!addList.isEmpty()) {
                postImageMapper.insert(addList);
            }
        }
    }

    @Override
    public void update(Post post) {
        if (post == null) return;
        updateById(post);
    }

    @Override
    public List<PostVO> queryByPostIds(List<Long> posts, Integer status, Long userId) {
        return postsMapper.queryByPostIds(posts, status, userId);
    }



    @Override
    public List<Post> findByIds(List<Long> ids) {
        return findListIn(ids, PostsPO::getId);
    }

    @Override
    public List<Post> queryMostHotPost(int limit) {
        LambdaQueryWrapper<PostsPO> wrapper = new LambdaQueryWrapper<PostsPO>().eq(PostsPO::getStatus, PostStatus.NORMAL.getCode())
                .orderByDesc(PostsPO::getHotScore)
                .last("limit " + limit);
        return getBaseMapper().selectList(wrapper).stream().map(this::toModel).toList();
    }





    @Override
    public List<PostVO> queryByPage(Long cursor, Long creatorId, Long userId, Integer status, Integer limit) {
        return postsMapper.queryByPage(cursor, creatorId, userId, status, limit);
    }

    @Override
    public List<Long> getPostsByTag(Long tagId, String cursor, Integer limit) {
        return postsMapper.getPostsByTag(tagId, cursor, limit);
    }

    @Override
    public List<Long> getHotPostsByTag(Long tagId, String cursor, Integer limit) {
        return postsMapper.getHotPostsByTag(tagId, cursor, limit);
    }

    @Override
    protected Post toModel(PostsPO po) {
        if (po == null) return null;
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

    @Override
    protected PostsPO toPO(Post post) {
        if (post == null) return null;
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
                .visibleScope(post.getVisibleScope() == null ? 1 : post.getVisibleScope().getCode())
                .likeCount(post.getLikeCount())
                .collectCount(post.getCollectCount())
                .replyCount(post.getReplyCount() != null ? post.getReplyCount().intValue() : 0)
                .hotScore(post.getHotScore() != null ? post.getHotScore().longValue() : 0L)
                .build();
    }
}


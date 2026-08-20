package com.summit.stp.post.infrastructure.persistence.repoImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.model.PostImage;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.domain.model.PostType;
import com.summit.stp.common.util.TrendDateUtil;
import com.summit.stp.post.domain.model.stats.PostContentStat;
import com.summit.stp.post.domain.repository.PostImageRepository;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostImageMapper;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.post.infrastructure.persistence.dto.PostTrendStatDTO;
import com.summit.stp.post.infrastructure.persistence.po.PostImagePO;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.summit.stp.post.api.vo.stats.PostContentStatsVO;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PostRepositoryImpl extends AbstractRepository<Post, PostsPO> implements PostRepository {
    private final PostsMapper postsMapper;
    private final PostImageMapper postImageMapper;
    private final PostImageRepository postImageRepository;


    public PostRepositoryImpl(PostsMapper postsMapper, PostImageMapper postImageMapper, PostImageRepository postImageRepository) {
        super(postsMapper);
        this.postsMapper = postsMapper;
        this.postImageMapper = postImageMapper;
        this.postImageRepository = postImageRepository;
    }




    @Override
    public Long savePost(Post post) {
        if (post == null) return null;
        Long postId = super.save(post, PostsPO::getId).longValue();

        if (post.getType() != null && PostType.IMAGE.getCode() == post.getType().getCode()) {
            // 保存帖子图片信息
            savePostImageInfo(post,postId);
        }
        return postId;
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
    public PostContentStat countContentStats(int days) {
        int limitDays = (days > 0 && days <= 60) ? days : 7;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(limitDays - 1);
        Timestamp startTimestamp = Timestamp.valueOf(startDate.atStartOfDay());

        List<PostTrendStatDTO> dtoList = postsMapper.selectContentStats(startTimestamp);
        Map<String, PostTrendStatDTO> resultMap = (dtoList != null) ? dtoList.stream()
                .filter(d -> d.getDateStr() != null)
                .collect(Collectors.toMap(PostTrendStatDTO::getDateStr, d -> d, (k1, k2) -> k1)) : Map.of();

        var postTrend = TrendDateUtil.buildTrendData(
                limitDays, resultMap, d -> d.getPostCnt() != null ? d.getPostCnt() : 0, 0
        );
        var commentTrend = TrendDateUtil.buildTrendData(
                limitDays, resultMap, d -> d.getCommentCnt() != null ? d.getCommentCnt() : 0, 0
        );
        var blockedTrend = TrendDateUtil.buildTrendData(
                limitDays, resultMap, d -> d.getBlockedCnt() != null ? d.getBlockedCnt() : 0, 0
        );

        return PostContentStat.builder()
                .dates(postTrend.dates())
                .postCountList(postTrend.values())
                .commentCountList(commentTrend.values())
                .blockedCountList(blockedTrend.values())
                .build();
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
                .unpassReason(po.getUnpassReason())
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
                .unpassReason(post.getUnpassReason())
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

    /**
     * 差集同步更新帖子图片信息
     *
     * @param post 帖子实体
     */
    private void savePostImageInfo(Post post,Long postId) {
        List<PostImage> mediaUrls = post.getUrls();
        if(mediaUrls == null)return;
        if (Post.isLimited(mediaUrls.size())) throw new BusinessException("图片数量已到达上限!");

        //1, 获取数据库中已存在的图片
        List<PostImage> dbImages = postImageRepository.findByPostId(postId);
        Map<String, PostImage> map = mediaUrls.stream().collect(Collectors.toMap(PostImage::getImageUrl, v -> v));
        Map<String, PostImage> dbMap = dbImages.stream().collect(Collectors.toMap(PostImage::getImageUrl, v -> v));


        //1, 差集同步
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


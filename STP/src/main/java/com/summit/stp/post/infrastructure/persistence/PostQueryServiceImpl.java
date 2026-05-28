package com.summit.stp.post.infrastructure.persistence;

import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.application.service.PostQueryService;
import com.summit.stp.post.application.vo.PostImageVO;
import com.summit.stp.post.application.vo.PostTagRelVO;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.application.vo.TagVO;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.domain.repository.PostImageRepository;
import com.summit.stp.post.domain.repository.PostTagRelRepository;
import com.summit.stp.post.domain.repository.TagRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.post.infrastructure.persistence.po.TagPO;
import com.summit.stp.shared.ThreadContext.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 帖子查询服务实现（CQRS 读模型实现），直接利用 Mybatis Mapper 完成高性能的多表联合查询
 */
@Service
@RequiredArgsConstructor
public class PostQueryServiceImpl implements PostQueryService {
    private final PostsMapper postsMapper;
    private final PostImageRepository postImageRepository;
    private final PostTagRelRepository postTagRelRepository;
    private final TagRepository tagRepository;
    private final PostCacheProvider postCacheProvider;

    @Override
    public List<PostVO> getPostPage(String cursor, Boolean self, Long creatorId, Integer status) {
        Long currentUserId = UserHolder.getUser().getId();
        int resolvedStatus = status == null ? PostStatus.NORMAL.getCode() : status;
        Long resolvedCreatorId = resolveCreatorId(self, creatorId, resolvedStatus, currentUserId);

        List<PostVO> postVOList = postsMapper.queryByPage(cursor, resolvedCreatorId, currentUserId, resolvedStatus);
        if (postVOList == null || postVOList.isEmpty()) {
            return Collections.emptyList();
        }
        //获取帖子id列表
        List<Long> postIds = postVOList.stream().map(PostVO::getId).toList();
        
        // 批量预热并加载缺失的缓存
        postCacheProvider.loadCache(postIds);
        
        // 批量查询附加信息
        Map<Long, List<PostImageVO>> imageMap = batchGetImages(postIds);
        Map<Long, List<TagVO>> tagMap = batchGetTags(postIds);
        
        // 装配数据
        populatePostMetadata(postVOList, imageMap, tagMap);
        
        return postVOList;
    }

    /**
     * 根据查询请求条件（如是否只查询个人、帖子状态等）解析实际应当过滤的创作者ID。
     * 规则：
     * 1. 若 self 为 true，强制仅查当前登录用户发布的内容。
     * 2. 若 status 为非 NORMAL 状态（如草稿、已删除等），强制限制只能查询当前登录用户自己发布的内容。
     *
     * @param self          是否只查询自己
     * @param creatorId     过滤的创作者ID
     * @param status        帖子状态
     * @param currentUserId 当前登录用户ID
     * @return 实际过滤的创作者用户ID
     */
    private Long resolveCreatorId(Boolean self, Long creatorId, int status, Long currentUserId) {
        if (self != null && self) {
            return currentUserId;
        }
        // 非 NORMAL 的状态只允许创作者本人查询
        if (status != PostStatus.NORMAL.getCode()) {
            if (creatorId == null || !creatorId.equals(currentUserId)) {
                return currentUserId;
            }
        }
        return creatorId;
    }

    /**
     * 批量查询这批帖子对应的图片列表。
     *
     * @param postIds 帖子ID列表
     * @return 帖子ID -> 图片VO列表的映射
     */
    private Map<Long, List<PostImageVO>> batchGetImages(List<Long> postIds) {
        return postImageRepository.findByIds(postIds);
    }

    /**
     * 批量查询这批帖子绑定的标签详情列表（做内存合并，规避循环 N+1 SQL 查询）。
     *
     * @param postIds 帖子ID列表
     * @return 帖子ID -> 标签VO列表的映射
     */
    private Map<Long, List<TagVO>> batchGetTags(List<Long> postIds) {
        List<PostTagRelVO> tagRels = postTagRelRepository.findByPostIds(postIds);
        if (tagRels == null || tagRels.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> tagIds = tagRels.stream().map(PostTagRelVO::getTagId).distinct().toList();
        List<TagPO> tagPOs = tagRepository.findByIds(tagIds);
        if (tagPOs == null || tagPOs.isEmpty()) {
            return Collections.emptyMap();
        }

        // 将 TagPO 转换为 TagVO List -> Map<tagId, TagVO>
        Map<Long, TagVO> tagVoMap = tagPOs.stream().collect(Collectors.toMap(
            TagPO::getId,
            po -> TagVO.builder()
                    .id(po.getId())
                    .tagName(po.getTagName())
                    .sort(po.getSort())
                    .useCount(po.getUseCount())
                    .status(po.getStatus())
                    .createTime(po.getCreateTime())
                    .build(),
            (existing, replacement) -> existing
        ));
        
        return tagRels.stream()
                .filter(rel -> tagVoMap.containsKey(rel.getTagId())) // 过滤掉不存在的标签
                .collect(Collectors.groupingBy(
                        PostTagRelVO::getPostId,
                        Collectors.mapping(rel -> tagVoMap.get(rel.getTagId()), Collectors.toList())
                ));
    }

    /**
     * 内存装配帖子的元数据信息：
     * 1. 填充 Redis 缓存中的点赞总数与收藏总数。
     * 2. 绑定图片资源列表。
     * 3. 绑定标签详情列表。
     *
     * @param postVOList 帖子VO列表
     * @param imageMap   已查到的图片映射关系
     * @param tagMap     已查到的标签映射关系
     */
    private void populatePostMetadata(List<PostVO> postVOList, Map<Long, List<PostImageVO>> imageMap, Map<Long, List<TagVO>> tagMap) {
        Long uid = UserHolder.getUser().getId();
        String COLLECT = PostCacheProvider.COLLECT;
        String LIKE = PostCacheProvider.LIKE;
        //获取当前帖子列表的点赞数和收藏数
        List<Long> list = postVOList.stream().map(PostVO::getId).toList();
        Map<Long,Map<String,Long>> likeAndCollectCountMap = postCacheProvider.getLikeAndCollectCount(list);
        Map<Long, Map<String, Boolean>> isCollectedOrLikedMap = postCacheProvider.getIsCollectedOrLiked(list, uid);
        for (PostVO vo : postVOList) {
            Long id = vo.getId();
            // 填充点赞和收藏数
            Map<String, Long> resultMap = likeAndCollectCountMap.get(id);
            Map<String, Boolean> isLikeAndCollectMap = isCollectedOrLikedMap.get(id);

            vo.setLikeCount(resultMap.getOrDefault(LIKE, 0L)-1);  //这里-1是因为set在初始化时,为了防止缓存穿透,设置的默认占位符,多占了一个单位

            vo.setCollectCount(resultMap.getOrDefault(COLLECT, 0L)-1);
            vo.setIsCollect(isLikeAndCollectMap.getOrDefault(COLLECT, false));
            vo.setIsLike(isLikeAndCollectMap.getOrDefault(LIKE, false));
            // 绑定图片
            vo.setMediaUrls(imageMap.getOrDefault(id, Collections.emptyList()));
            
            // 绑定标签
            vo.setTags(tagMap.getOrDefault(id, Collections.emptyList()));
        }
    }
}

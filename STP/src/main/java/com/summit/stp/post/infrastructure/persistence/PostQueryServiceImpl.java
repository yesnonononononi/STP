package com.summit.stp.post.infrastructure.persistence;

import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.application.service.PostQueryService;
import com.summit.stp.post.application.vo.PostImageVO;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.application.vo.TagVO;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.domain.model.PostTag;
import com.summit.stp.post.domain.model.Tag;
import com.summit.stp.post.domain.repository.*;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.user.application.UserFiller;
import com.summit.stp.user.application.vo.UserSimpleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final PostRepository postRepository;
    private final UserFiller userFiller;

    @Override
    public List<PostVO> getPostPage(Long cursor, Boolean self, Long creatorId, Integer status, Integer limit) {
        Long currentUserId = UserHolder.getUser().getId();
        Long resolvedCreatorId = resolveCreatorId(self, creatorId, status != null ? status : PostStatus.NORMAL.getCode(), currentUserId);
        List<PostVO> postVOList = postsMapper.queryByPage(cursor, resolvedCreatorId, currentUserId, status, limit);
        return resolveExtraInfo(postVOList);
    }

    @Override
    public List<PostVO> getMyCollectPostList(Long targetUserId, String cursor) {
        Long currentUserId = UserHolder.getUser().getId();
        Long resolvedTargetUserId = targetUserId != null ? targetUserId : currentUserId;
        if (!checkListVisiblePermission(resolvedTargetUserId, currentUserId)) {
            return Collections.emptyList();
        }
        List<Long> posts = postCollectRepository.findByUserId(resolvedTargetUserId, cursor);
        if (posts.isEmpty()) {
            return Collections.emptyList();
        }
        List<PostVO> list = postRepository.queryByPostIds(posts, PostStatus.NORMAL.getCode(), currentUserId);
        return resolveExtraInfo(list);
    }

    @Override
    public List<PostVO> getMyLikePostList(Long targetUserId, String cursor) {
        Long currentUserId = UserHolder.getUser().getId();
        Long resolvedTargetUserId = targetUserId != null ? targetUserId : currentUserId;
        if (!checkListVisiblePermission(resolvedTargetUserId, currentUserId)) {
            return Collections.emptyList();
        }
        List<Long> posts = postLikeRepository.findByUserId(resolvedTargetUserId, cursor);
        if (posts.isEmpty()) {
            return Collections.emptyList();
        }
        List<PostVO> list = postRepository.queryByPostIds(posts, PostStatus.NORMAL.getCode(), currentUserId);
        return resolveExtraInfo(list);
    }

    private boolean checkListVisiblePermission(Long targetUserId, Long currentUserId) {
        // 预留逻辑：便于后期拓展用户设置不准他人查看收藏/点赞信息的功能，默认允许
        return true;
    }

    @Override
    public PostVO findById(Long id, Long uid, Integer status) {
        List<PostVO> postVOS = postRepository.queryByPostIds(List.of(id), status, uid);
        return postVOS.isEmpty() ? null : resolveExtraInfo(postVOS).getFirst();
    }


    /**
     * 填充剩余帖子缺失信息(帖子相关图片,标签,缓存预热,点赞,收藏等)
     * @param postVOList 待加工帖子列表(包含发帖用户信息,帖子基本信息等核心信息)
     * @return 帖子列表
     */
    private  List<PostVO> resolveExtraInfo(List<PostVO> postVOList){
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

        // 批量装配发帖人用户信息
        Set<Long> creatorIds = postVOList.stream()
                .map(PostVO::getCreatorId)
                .collect(Collectors.toSet());
        Map<Long, UserSimpleVO> userMap = userFiller.fillUsers(creatorIds);
        postVOList.forEach(vo -> vo.setPublisher(userMap.get(vo.getCreatorId())));

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
        List<PostTag> tagRels = postTagRelRepository.findByPostIds(postIds);
        if (tagRels == null || tagRels.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> tagIds = tagRels.stream().map(PostTag::getTagId).distinct().toList();
        List<Tag> tags = tagRepository.findByIds(tagIds);
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyMap();
        }

        // 将 Tag 转换为 TagVO List -> Map<tagId, TagVO>
        Map<Long, TagVO> tagVoMap = tags.stream().collect(Collectors.toMap(
            Tag::getId,
            tag -> TagVO.builder()
                    .id(tag.getId())
                    .tagName(tag.getTagName())
                    .sort(tag.getSort())
                    .useCount(tag.getUseCount())
                    .status(tag.getStatus())
                    .createTime(tag.getCreateTime())
                    .build(),
            (existing, replacement) -> existing
        ));
        
        return tagRels.stream()
                .filter(rel -> tagVoMap.containsKey(rel.getTagId())) // 过滤掉不存在的标签
                .collect(Collectors.groupingBy(
                        PostTag::getPostId,
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

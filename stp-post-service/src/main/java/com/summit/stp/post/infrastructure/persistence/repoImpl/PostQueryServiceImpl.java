package com.summit.stp.post.infrastructure.persistence.repoImpl;

import cn.hutool.core.util.StrUtil;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.elasticsearch.service.PostQuerySupport;
import com.summit.stp.post.api.vo.PostSimpleVO;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.user.api.vo.UserSettingVO;
import com.summit.stp.user.api.vo.UserSimpleVO;
import com.summit.stp.common.constants.CacheFieldConstants;
import com.summit.stp.user.api.client.UserFeignClient;
import com.summit.stp.elasticsearch.document.PostDocument;
import com.summit.stp.post.api.dto.request.QueryPostListPageRequest;
import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.application.vo.PostImageVO;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.domain.repository.*;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.tag.application.service.TagCacheProvider;
import com.summit.stp.tag.application.vo.TagVO;
import com.summit.stp.tag.domain.model.PostTag;
import com.summit.stp.tag.domain.model.Tag;
import com.summit.stp.tag.domain.repository.PostTagRelRepository;
import com.summit.stp.tag.domain.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import java.util.function.BiFunction;

/**
 * 帖子查询服务实现（CQRS 读模型实现），直接利用 Mybatis Mapper 完成高性能的多表联合查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostQueryServiceImpl implements com.summit.stp.post.application.service.PostQueryService {
    private final PostImageRepository postImageRepository;
    private final PostTagRelRepository postTagRelRepository;
    private final TagRepository tagRepository;
    private final PostCacheProvider postCacheProvider;
    private final TagCacheProvider tagCacheProvider;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final PostRepository postRepository;
    private final UserFeignClient userFeignClient;
    private final PostMetadataAssembler metadataAssembler;
    private final PostQuerySupport postQuerySupport;

    @Override
    public List<PostVO> getPostPage(Long cursor, Boolean self, Long creatorId, Integer status, String orderType, Integer limit) {
        Long currentUserId = UserHolder.getUser().getId();
        Long resolvedCreatorId = resolveCreatorId(self, creatorId, status != null ? status : PostStatus.NORMAL.getCode(), currentUserId);

        // 判定是否可以使用 Redis 缓存的全局公开分页：非自己查询，非指定作者查询，状态为 NORMAL (或空)
        boolean isGlobalQuery = resolvedCreatorId == null && (status == null || status == PostStatus.NORMAL.getCode());

        if (isGlobalQuery) {
            try {
                return globalCacheQuery(cursor, orderType, limit);
            } catch (Exception e) {
                log.warn("【帖子模块】分页获取帖子失败，动作：查询Redis缓存降级读库", e);
            }
        }
        List<PostVO> postVOList = postRepository.queryByPage(cursor, resolvedCreatorId, currentUserId, status, limit);
        if (Objects.equals(resolvedCreatorId, currentUserId)) {
            try {
                UserSettingVO setting = userFeignClient.getUserSetting(currentUserId).getData();
                if (setting != null && setting.getShowDelPost() == 0) {
                    postVOList = postVOList.stream()
                            .filter(vo -> vo.getStatus() != null && vo.getStatus() != PostStatus.DELETED.getCode())
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.error("【帖子模块】查看自己的帖子过滤异常，动作：查询用户偏好配置", e);
            }
        }
        return resolveExtraInfo(postVOList);
    }


    @Override
    public List<PostVO> getByPostIds(List<Long> ids) {
        List<PostVO> postVOS = fetchPostsByIdsFromCache(ids);
        return resolveExtraInfo(postVOS);

    }


    @Override
    public List<PostVO> getMyCollectPostList(Long targetUserId, String cursor) {
        return getInteractPostList(targetUserId, cursor, postCollectRepository::findByUserId);
    }

    @Override
    public List<PostVO> getMyLikePostList(Long targetUserId, String cursor) {
        return getInteractPostList(targetUserId, cursor, postLikeRepository::findByUserId);
    }


    @Override
    public List<PostVO> getFollowPostList(QueryPostListPageRequest request) {
        return List.of();
    }


    @Override
    public PostVO findById(Long id, Long uid, Integer status) {
        try {
            PostVO cachedVO = postCacheProvider.getPostContent(id);
            if (cachedVO != null) {
                log.info("【帖子查询-临时日志】走缓存（详情Hash），postId={}", id);
                if (status != null && !status.equals(cachedVO.getStatus())) {
                    return null;
                }
                List<PostVO> list = new ArrayList<>();
                list.add(cachedVO);
                return resolveExtraInfo(list).getFirst();
            }
            log.info("【帖子查询-临时日志】详情Hash未命中，降级走DB，postId={}", id);
        } catch (Exception e) {
            log.warn("【帖子模块】查询帖子详情获取缓存异常，动作：查询Redis详情缓存", e);
        }

        List<PostVO> postVOS = postRepository.queryByPostIds(List.of(id), status, uid);
        if (postVOS.isEmpty()) {
            return null;
        }
        return resolveExtraInfo(postVOS).getFirst();
    }

    @Override
    public List<PostVO> searchPost(String keyWord, Integer page) {
        if (StrUtil.isBlank(keyWord)) return List.of();
        List<Long> list = postQuerySupport.findByKeyWords(keyWord,Objects.requireNonNullElse(page,1)).stream().map(PostDocument::getId).toList();
        List<PostVO> byPostIds = this.getByPostIds(list);
        return Objects.requireNonNullElse(byPostIds, List.of());
    }


    private List<PostVO> globalCacheQuery(Long cursor, String orderType, Integer limit) {

        long start = 0;
        Set<Long> postIds;
        Long rank;
        // 热门 ZSet 分页：限制翻页深度 5000。若 cursor 不为空，查 rank 计算 start，找不到说明超出或被淘汰，直接返回空列表
        if (PostConstants.Business.ORDER_TYPE_HOT.equalsIgnoreCase(orderType)) {
            if (cursor != null) {
                rank = postCacheProvider.getRankFromHot(cursor);
                if (rank == null) {
                    return Collections.emptyList();
                }
                start = rank + 1;
            }
            postIds = postCacheProvider.getPostIdsFromHot((int) start, limit);
            log.info("【热榜SET查询-缓存命中】{}条", postIds.size());
        } else {
            if (cursor != null) {
                rank = postCacheProvider.getRankFromNewest(cursor);
                if (rank == null) {
                    return Collections.emptyList();
                }
                start = rank + 1;
            }
            postIds = postCacheProvider.getPostIdsFromNewest((int) start, limit);
            log.info("【新帖SET查询-缓存命中】 {}条", postIds.size());
        }


        if (!postIds.isEmpty()) {
            //如果从缓存中拉取的状态帖子不为空,则拉取帖子实体
            List<PostVO> cachedVOList = fetchPostsByIdsFromCache(new ArrayList<>(postIds));
            //按指定id顺序对帖子排序
            List<PostVO> sortedVOList = sortVoListByOrder(cachedVOList, postIds);
            return resolveExtraInfo(sortedVOList);
        }
        throw new RuntimeException("缓存未命中帖子");


    }

    /**
     * 批量获取帖子主体：优先 detail Hash 缓存，缺失的降级查 DB 并回写缓存。
     * loadCache 内部已做 pipeline 批量检查 + 缺失的从 DB 拉取 PO 填充 Hash，
     * 因此此方法调用后绝大多数情况全部命中缓存，不再打 DB。
     */
    private List<PostVO> fetchPostsByIdsFromCache(List<Long> postIdList) {
        if (postIdList == null || postIdList.isEmpty()) {
            return Collections.emptyList();
        }
        // 1. 预热 detail Hash（内部 pipeline 批量检查存在性，缺失的从 DB 拉取 PO 填充）
        postCacheProvider.loadCache(postIdList);
        // 2. 批量从 detail Hash 读取
        Map<Long, PostVO> cachedMap = postCacheProvider.batchGetPostContents(postIdList);
        // 3. 仍缺失的（极少，如 loadCache 异常或并发删除）降级查 DB
        List<Long> missing = postIdList.stream()
                .filter(id -> !cachedMap.containsKey(id))
                .toList();
        if (!missing.isEmpty()) {
            log.info("【帖子查询-缓存未命中】查询数据库 {} 条", missing.size());
            Long uid = UserHolder.getUser().getId();
            List<PostVO> dbList = postRepository.queryByPostIds(missing, PostStatus.NORMAL.getCode(), uid);
            for (PostVO vo : dbList) {
                cachedMap.put(vo.getId(), vo);
            }
        }
        // 4. 按 postIdList 顺序返回（过滤掉最终仍为 null 的）
        return postIdList.stream()
                .map(cachedMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 抽取通用的用户互动帖子列表查询（点赞/收藏模板方法）
     */
    private List<PostVO> getInteractPostList(Long targetUserId, String cursor, BiFunction<Long, String, List<Long>> postIdsFetcher) {
        Long currentUserId = UserHolder.getUser().getId();
        Long resolvedTargetUserId = targetUserId != null ? targetUserId : currentUserId;
        if (!checkListVisiblePermission(resolvedTargetUserId, currentUserId)) {
            return Collections.emptyList();
        }
        List<Long> posts = postIdsFetcher.apply(resolvedTargetUserId, cursor);
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyList();
        }
        List<PostVO> list = postRepository.queryByPostIds(posts, PostStatus.NORMAL.getCode(), currentUserId);
        return resolveExtraInfo(list);
    }

    /**
     * 填充剩余帖子缺失信息(帖子相关图片,标签,缓存预热,点赞,收藏等)
     */
    private List<PostVO> resolveExtraInfo(List<PostVO> postVOList) {
        if (postVOList == null || postVOList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> postIds = postVOList.stream().map(PostVO::getId).toList();
        Map<Long, List<PostImageVO>> imageMap = batchGetImages(postIds);
        Map<Long, List<TagVO>> tagMap = batchGetTags(postIds);
        Long uid = UserHolder.getUser().getId();

        // 步骤1: 获取附加数据（优先缓存，降级 DB）
        PostMetadataAssembler.PostExtraData extraData = fetchExtraData(postIds, uid);
        // 步骤2: 统一装配
        metadataAssembler.assemble(postVOList, extraData, imageMap, tagMap);
        // 步骤3: 填充发布者信息
        fillPublishers(postVOList);
        return postVOList;
    }

    /**
     * 从 Redis 缓存批量获取帖子附加数据，异常时降级查 DB
     */
    private PostMetadataAssembler.PostExtraData fetchExtraData(List<Long> postIds, Long uid) {
        try {
            postCacheProvider.loadCache(postIds);
            Map<Long, Map<String, Long>> countMap = postCacheProvider.getLikeAndCollectCount(postIds);
            Map<Long, Map<String, Boolean>> statusMap = postCacheProvider.getIsCollectedOrLiked(postIds, uid);
            Map<Long, Long> replyCountMap = postCacheProvider.getReplyCounts(postIds);
            Map<Long, Long> viewCountMap = postCacheProvider.getViewCounts(postIds);

            Map<Long, Long> likeCounts = new HashMap<>(postIds.size());
            Map<Long, Long> collectCounts = new HashMap<>(postIds.size());
            for (Map.Entry<Long, Map<String, Long>> e : countMap.entrySet()) {
                likeCounts.put(e.getKey(), e.getValue().getOrDefault(CacheFieldConstants.LIKE_COUNT, 0L));
                collectCounts.put(e.getKey(), e.getValue().getOrDefault(CacheFieldConstants.COLLECT_COUNT, 0L));
            }

            Map<Long, Boolean> likeStatus = new HashMap<>(postIds.size());
            Map<Long, Boolean> collectStatus = new HashMap<>(postIds.size());
            for (Map.Entry<Long, Map<String, Boolean>> e : statusMap.entrySet()) {
                likeStatus.put(e.getKey(), e.getValue().getOrDefault(CacheFieldConstants.INTERACTION_LIKE, false));
                collectStatus.put(e.getKey(), e.getValue().getOrDefault(CacheFieldConstants.INTERACTION_COLLECT, false));
            }

            return new PostMetadataAssembler.PostExtraData(
                    likeCounts, collectCounts, likeStatus, collectStatus, replyCountMap, viewCountMap
            );
        } catch (Exception e) {
            log.warn("【帖子模块】读取帖子数据缓存异常触发降级，动作：读取Redis计数降级查DB", e);
            return fetchExtraDataFromDb(postIds, uid);
        }
    }

    /**
     * Redis 不可用时，从数据库降级获取帖子附加数据
     */
    private PostMetadataAssembler.PostExtraData fetchExtraDataFromDb(List<Long> postIds, Long uid) {
        try {
            Map<Long, List<Long>> likesMap = postLikeRepository.findUserIdsByPostIds(postIds);
            Map<Long, List<Long>> collectsMap = postCollectRepository.findUserIdsByPostIds(postIds);
            List<PostVO> dbPosts = postRepository.queryByPostIds(postIds, null, uid);

            Map<Long, Long> likeCounts = new HashMap<>(postIds.size());
            Map<Long, Long> collectCounts = new HashMap<>(postIds.size());
            Map<Long, Boolean> likeStatus = new HashMap<>(postIds.size());
            Map<Long, Boolean> collectStatus = new HashMap<>(postIds.size());

            Map<Long, PostVO> dbPostMap = dbPosts == null ? Collections.emptyMap() :
                    dbPosts.stream().collect(Collectors.toMap(PostVO::getId, vo -> vo, (v1, v2) -> v1));

            for (Long postId : postIds) {
                PostVO postVO = dbPostMap.get(postId);
                long likeCount = postVO != null && postVO.getLikeCount() != null ? postVO.getLikeCount() : 0L;
                long collectCount = postVO != null && postVO.getCollectCount() != null ? postVO.getCollectCount() : 0L;

                List<Long> likeUsers = likesMap.getOrDefault(postId, Collections.emptyList());
                List<Long> collectUsers = collectsMap.getOrDefault(postId, Collections.emptyList());

                likeCounts.put(postId, likeCount);
                collectCounts.put(postId, collectCount);
                likeStatus.put(postId, likeUsers.contains(uid));
                collectStatus.put(postId, collectUsers.contains(uid));
            }

            Map<Long, Long> replyCountMap = dbPosts == null ? Collections.emptyMap() :
                    dbPosts.stream().collect(Collectors.toMap(
                            PostVO::getId,
                            vo -> vo.getReplyCount() != null ? vo.getReplyCount() : 0L,
                            (v1, v2) -> v1
                    ));
            Map<Long, Long> viewCountMap = dbPosts == null ? Collections.emptyMap() :
                    dbPosts.stream().collect(Collectors.toMap(
                            PostVO::getId,
                            vo -> vo.getViewCount() != null ? vo.getViewCount() : 0L,
                            (v1, v2) -> v1
                    ));

            return new PostMetadataAssembler.PostExtraData(
                    likeCounts, collectCounts, likeStatus, collectStatus, replyCountMap, viewCountMap
            );
        } catch (Exception ex) {
            log.error("【帖子模块】数据库降级查询失败，动作：组装帖子附加数据", ex);
            return PostMetadataAssembler.PostExtraData.empty();
        }
    }

    /**
     * 批量填充发帖人用户信息
     */
    private void fillPublishers(List<PostVO> postVOList) {
        Set<Long> creatorIds = postVOList.stream()
                .map(PostVO::getCreatorId)
                .collect(Collectors.toSet());
        Map<Long, UserSimpleVO> userMap = userFeignClient.findSimpleUserByIds(creatorIds).getData();
        postVOList.forEach(vo -> vo.setPublisher(userMap.get(vo.getCreatorId())));
    }

    private boolean checkListVisiblePermission(Long targetUserId, Long currentUserId) {
        return true;
    }

    /**
     * 根据查询请求条件解析实际应当过滤的创作者ID。
     * 1. 若 self 为 true，强制仅查当前登录用户发布的内容。
     * 2. 若 status 为非 NORMAL 状态，强制限制只能查询当前登录用户自己发布的内容。
     */
    private Long resolveCreatorId(Boolean self, Long creatorId, int status, Long currentUserId) {
        if (self != null && self) {
            return currentUserId;
        }
        if (status != PostStatus.NORMAL.getCode()) {
            if (creatorId == null || !creatorId.equals(currentUserId)) {
                return currentUserId;
            }
        }
        return creatorId;
    }

    private Map<Long, List<PostImageVO>> batchGetImages(List<Long> postIds) {
        return postImageRepository.findByIds(postIds);
    }

    private Map<Long, List<TagVO>> batchGetTags(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, List<TagVO>> result = new HashMap<>(postIds.size());
        // 1. 从 detail Hash 批量读取 tagIds 字段
        Map<Long, String> tagIdsMap = postCacheProvider.batchGetTagIds(postIds);
        // 2. 收集所有 tagId，批量查 tag:detail 缓存
        Set<Long> allTagIds = new LinkedHashSet<>();
        Map<Long, List<Long>> postToTagIds = new HashMap<>(postIds.size());
        List<Long> missingPostIds = new ArrayList<>();
        for (Long postId : postIds) {
            String tagIdsStr = tagIdsMap.get(postId);
            if (tagIdsStr != null) {
                List<Long> ids = parseTagIds(tagIdsStr);
                postToTagIds.put(postId, ids);
                allTagIds.addAll(ids);
            } else {
                missingPostIds.add(postId);
            }
        }
        // 3. detail Hash 缺失 tagIds 的帖子，从 DB 查 postTagRel（说明 detail Hash 未预热或被淘汰）
        if (!missingPostIds.isEmpty()) {
            List<PostTag> tagRels = postTagRelRepository.findByPostIds(missingPostIds);
            Map<Long, List<Long>> dbTagIdsMap = tagRels == null ? Collections.emptyMap() :
                    tagRels.stream().collect(Collectors.groupingBy(
                            PostTag::getPostId,
                            Collectors.mapping(PostTag::getTagId, Collectors.toList())
                    ));
            for (Long postId : missingPostIds) {
                List<Long> ids = dbTagIdsMap.getOrDefault(postId, Collections.emptyList());
                postToTagIds.put(postId, ids);
                allTagIds.addAll(ids);
            }
        }
        // 4. 批量获取 tag 详情（缓存优先，DB 兜底）
        Map<Long, TagVO> tagVoMap = batchGetTagDetails(new ArrayList<>(allTagIds));
        // 5. 按帖子组装
        for (Long postId : postIds) {
            List<Long> ids = postToTagIds.getOrDefault(postId, Collections.emptyList());
            List<TagVO> tags = ids.stream()
                    .map(tagVoMap::get)
                    .filter(Objects::nonNull)
                    .toList();
            result.put(postId, tags);
        }
        return result;
    }

    /**
     * 批量获取标签详情：缓存优先 → DB 兜底 → 回写缓存
     */
    private Map<Long, TagVO> batchGetTagDetails(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, TagVO> result = new HashMap<>(tagIds.size());
        // 缓存
        try {
            Map<Long, TagVO> cached = tagCacheProvider.batchGetTagDetails(tagIds);
            result.putAll(cached);
        } catch (Exception e) {
            log.warn("【帖子模块】读取标签详情缓存异常，降级查DB", e);
        }
        // 缺失的查 DB
        List<Long> missing = tagIds.stream().filter(id -> !result.containsKey(id)).distinct().toList();
        if (!missing.isEmpty()) {
            List<Tag> tags = tagRepository.findByIds(missing);
            if (tags != null) {
                List<TagVO> dbTags = new ArrayList<>();
                for (Tag tag : tags) {
                    TagVO vo = TagVO.builder()
                            .id(tag.getId() == null ? null : tag.getId().toString())
                            .tagName(tag.getTagName())
                            .sort(tag.getSort())
                            .useCount(tag.getUseCount())
                            .status(tag.getStatus())
                            .createTime(tag.getCreateTime())
                            .build();
                    result.put(tag.getId(), vo);
                    dbTags.add(vo);
                }
                // 回写缓存
                if (!dbTags.isEmpty()) {
                    try {
                        tagCacheProvider.batchSaveTagDetails(dbTags);
                    } catch (Exception e) {
                        log.warn("【帖子模块】回写标签详情缓存异常", e);
                    }
                }
            }
        }
        return result;
    }


    private List<Long> parseTagIds(String tagIdsStr) {
        if (tagIdsStr == null || tagIdsStr.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tagIdsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
    }

    /**
     * 根据指定的 ID 顺序对帖子列表进行排序
     */
    private List<PostVO> sortVoListByOrder(List<PostVO> list, Set<Long> orderIds) {
        if (list == null || list.isEmpty() || orderIds == null || orderIds.isEmpty()) {
            return list;
        }
        Map<Long, PostVO> voMap = list.stream().collect(Collectors.toMap(PostVO::getId, vo -> vo));
        List<PostVO> sorted = new ArrayList<>(list.size());
        for (Long id : orderIds) {
            PostVO vo = voMap.get(id);
            if (vo != null) {
                sorted.add(vo);
            }
        }
        return sorted;
    }

    @Override
    public PostSimpleVO findSimplePostById(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return null;
        }
        return PostSimpleVO.builder()
                .id(post.getId())
                .creatorId(post.getCreatorId())
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus() != null ? post.getStatus().getCode() : null)
                .build();
    }
}


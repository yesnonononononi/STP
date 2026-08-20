package com.summit.stp.post.application.service.impl;

import com.summit.stp.common.constants.CacheFieldConstants;
import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.domain.repository.PostCollectRepository;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.infrastructure.persistence.repoImpl.PostMetadataAssembler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@RequiredArgsConstructor
@Component
@Slf4j
public class PostMediaInfoAssembler {
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final PostRepository postRepository;
    private final PostCacheProvider postCacheProvider;








    /**
     *  assemble meta info to post from database or cache </br>
     * 1, like</br>
     * 2, collect</br>
     * 3, isLike</br>
     * 4, isCollect</br>
     * 5, reply</br>
     * 6, viewcount</br>
     * @param postIds post List
     * @param  uid userId
     */
    public PostMetadataAssembler.PostExtraData fetchExtraData(List<Long> postIds, Long uid) {
        try {
            return fetchExtraDataFromCache(postIds,uid);
        } catch (Exception e) {
            log.warn("【帖子模块】读取帖子数据缓存异常触发降级，动作：读取Redis计数降级查DB", e);
            return fetchExtraDataFromDb(postIds, uid);
        }
    }





    /**
     *  assemble meta info to post from database </br>
     * 1, like</br>
     * 2, collect</br>
     * 3, isLike</br>
     * 4, isCollect</br>
     * 5, reply</br>
     * 6, viewcount</br>
     * @param postIds post List
     * @param  uid userId
     */
    public PostMetadataAssembler.PostExtraData fetchExtraDataFromDb(List<Long> postIds, Long uid) {
        try {
            Map<Long, List<Long>> likesMap = postLikeRepository.findUserIdsByPostIds(postIds);
            Map<Long, List<Long>> collectsMap = postCollectRepository.findUserIdsByPostIds(postIds);
            List<PostVO> dbPosts = postRepository.queryByPostIds(postIds, null, uid);

          return assemble(likesMap,collectsMap,dbPosts,postIds,uid);
        } catch (Exception ex) {
            log.error("【帖子模块】数据库查询失败，动作：组装帖子附加数据", ex);
            return PostMetadataAssembler.PostExtraData.empty();
        }
    }
    /**
     *  assemble meta info to post from cache </br>
     * 1, like</br>
     * 2, collect</br>
     * 3, isLike</br>
     * 4, isCollect</br>
     * 5, reply</br>
     * 6, viewcount</br>
     * @param postIds post List
     * @param  uid userId
     */
    public PostMetadataAssembler.PostExtraData fetchExtraDataFromCache(List<Long> postIds, Long uid) {
            postCacheProvider.loadCache(postIds);
            Map<Long, Map<String, Long>> countMap = postCacheProvider.getLikeAndCollectCount(postIds);
            Map<Long, Map<String, Boolean>> statusMap = postCacheProvider.getIsCollectedOrLiked(postIds, uid);
            Map<Long, Long> replyCountMap = postCacheProvider.getReplyCounts(postIds);
            Map<Long, Long> viewCountMap = postCacheProvider.getViewCounts(postIds);
            return assembleFromCache(postIds,countMap,statusMap,replyCountMap,viewCountMap);
    }



    private PostMetadataAssembler.PostExtraData assembleFromCache(List<Long> postIds,
                                                                  Map<Long, Map<String, Long>> countMap,
                                                                  Map<Long, Map<String, Boolean>> statusMap,
                                                                  Map<Long, Long> replyCountMap,Map<Long, Long> viewCountMap
    ){
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
    }
    private PostMetadataAssembler.PostExtraData assemble(Map<Long, List<Long>> likesMap,Map<Long, List<Long>> collectsMap,List<PostVO> dbPosts,List<Long> postIds, Long uid){

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

    }
}

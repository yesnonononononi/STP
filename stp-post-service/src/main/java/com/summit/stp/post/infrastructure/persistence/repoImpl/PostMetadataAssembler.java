package com.summit.stp.post.infrastructure.persistence.repoImpl;

import com.summit.stp.post.application.vo.PostImageVO;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.tag.application.vo.TagVO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 帖子元数据组装器，统一处理正常路径和降级路径的附加数据装配，
 * 消除原 populatePostMetadata（7参数）和 fallbackMetadataFromDb 的重复代码。
 */
@Component
public class PostMetadataAssembler {

    /**
     * 帖子附加数据包，封装所有需要装配到 PostVO 的元数据来源
     *
     * @param likeCounts    帖子ID -> 点赞数
     * @param collectCounts 帖子ID -> 收藏数
     * @param likeStatus    帖子ID -> 当前用户是否已点赞
     * @param collectStatus 帖子ID -> 当前用户是否已收藏
     * @param replyCounts   帖子ID -> 回复数
     * @param viewCounts    帖子ID -> 浏览数
     */
    public record PostExtraData(
            Map<Long, Long> likeCounts,
            Map<Long, Long> collectCounts,
            Map<Long, Boolean> likeStatus,
            Map<Long, Boolean> collectStatus,
            Map<Long, Long> replyCounts,
            Map<Long, Long> viewCounts
    ) {
        public static PostExtraData empty() {
            return new PostExtraData(
                    Collections.emptyMap(), Collections.emptyMap(),
                    Collections.emptyMap(), Collections.emptyMap(),
                    Collections.emptyMap(), Collections.emptyMap()
            );
        }
    }

    /**
     * 统一组装帖子元数据：计数、互动状态、回复数、浏览数、图片、标签。
     * 正常路径和 DB 降级路径共用此方法，仅 PostExtraData 数据来源不同。
     *
     * @param posts    待装配的帖子列表
     * @param data     附加数据包
     * @param imageMap 帖子ID -> 图片列表
     * @param tagMap   帖子ID -> 标签列表
     */
    public void assemble(
            List<PostVO> posts,
            PostExtraData data,
            Map<Long, List<PostImageVO>> imageMap,
            Map<Long, List<TagVO>> tagMap
    ) {
        for (PostVO vo : posts) {
            Long id = vo.getId();
            vo.setLikeCount(data.likeCounts().getOrDefault(id, 0L));
            vo.setCollectCount(data.collectCounts().getOrDefault(id, 0L));
            vo.setIsLike(Boolean.TRUE.equals(data.likeStatus().get(id)));
            vo.setIsCollect(Boolean.TRUE.equals(data.collectStatus().get(id)));
            vo.setReplyCount(data.replyCounts().getOrDefault(id, 0L));
            vo.setViewCount(data.viewCounts().getOrDefault(id, 0L));
            vo.setMediaUrls(imageMap != null ? imageMap.getOrDefault(id, Collections.emptyList()) : Collections.emptyList());
            vo.setTags(tagMap != null ? tagMap.getOrDefault(id, Collections.emptyList()) : Collections.emptyList());
        }
    }
}

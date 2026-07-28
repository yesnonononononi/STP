package com.summit.stp.post.api;

import com.summit.stp.common.application.vo.PostSimpleVO;
import com.summit.stp.common.result.Result;
import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子内部接口 - 供其他微服务通过 Feign 调用
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/post/internal")
public class PostInternalController {

    private final PostRepository postRepository;
    private final PostCacheProvider postCacheProvider;

    /**
     * 查询帖子简要信息（供 IM/评论等服务获取帖子元数据）
     */
    @GetMapping("/simple/{id}")
    public Result<PostSimpleVO> findSimplePostById(@PathVariable Long id) {
        Post post = postRepository.findById(id);
        if (post == null) {
            return Result.success(null);
        }
        return Result.success(PostSimpleVO.builder()
                .id(post.getId())
                .creatorId(post.getCreatorId())
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus() != null ? post.getStatus().getCode() : null)
                .build());
    }

    /**
     * 增加帖子回复数（Redis 缓存层）
     */
    @PostMapping("/replyCount/incr")
    public Result<Void> incrReplyCount(@RequestParam Long postId) {
        postCacheProvider.incrReplyCount(postId);
        return Result.success();
    }

    /**
     * 减少帖子回复数（Redis 缓存层）
     */
    @PostMapping("/replyCount/decr")
    public Result<Void> decrReplyCount(@RequestParam Long postId) {
        postCacheProvider.decrReplyCount(postId);
        return Result.success();
    }
}

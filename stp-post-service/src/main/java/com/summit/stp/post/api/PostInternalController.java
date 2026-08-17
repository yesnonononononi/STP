package com.summit.stp.post.api;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.post.api.vo.PostSimpleVO;
import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.application.service.PostQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子内部接口 - 供其他微服务通过 Feign 调用
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/post/internal")
public class PostInternalController {

    private final PostQueryService postQueryService;
    private final PostCacheProvider postCacheProvider;

    /**
     * 查询帖子简要信息（供 IM/评论等服务获取帖子元数据）
     */
    @GetMapping("/simple/{id}")
    public Result<PostSimpleVO> findSimplePostById(@PathVariable Long id) {
        return Result.success(postQueryService.findSimplePostById(id));
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

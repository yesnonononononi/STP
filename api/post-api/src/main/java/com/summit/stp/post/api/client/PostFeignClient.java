package com.summit.stp.post.api.client;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.post.api.vo.PostSimpleVO;
import com.summit.stp.post.api.vo.stats.PostContentStatsVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Post 微服务 Feign 客户端
 */
@FeignClient(name = "stp-post-service", contextId = "postFeignClient")
public interface PostFeignClient {

    @GetMapping("/post/internal/simple/{id}")
    Result<PostSimpleVO> findSimplePostById(@PathVariable("id") Long id);

    @PostMapping("/post/internal/replyCount/incr")
    Result<Void> incrReplyCount(@RequestParam("postId") Long postId);

    @PostMapping("/post/internal/replyCount/decr")
    Result<Void> decrReplyCount(@RequestParam("postId") Long postId);

    @GetMapping("/post/internal/stats/content")
    Result<PostContentStatsVO> getContentStats(@RequestParam("period") String period);
}

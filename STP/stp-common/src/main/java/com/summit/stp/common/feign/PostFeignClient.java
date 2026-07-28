package com.summit.stp.common.feign;

import com.summit.stp.common.application.vo.PostSimpleVO;
import com.summit.stp.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Post 微服务 Feign 客户端 - 供其他微服务调用帖子查询和计数能力
 */
@FeignClient(name = "stp-post-service", contextId = "postFeignClient")
public interface PostFeignClient {

    /**
     * 根据帖子 ID 查询帖子简要信息（供其他服务获取帖子元数据）
     */
    @GetMapping("/post/internal/simple/{id}")
    Result<PostSimpleVO> findSimplePostById(@PathVariable("id") Long id);

    /**
     * 增加帖子回复数（Redis 缓存层）
     */
    @PostMapping("/post/internal/replyCount/incr")
    Result<Void> incrReplyCount(@RequestParam("postId") Long postId);

    /**
     * 减少帖子回复数（Redis 缓存层）
     */
    @PostMapping("/post/internal/replyCount/decr")
    Result<Void> decrReplyCount(@RequestParam("postId") Long postId);
}

package com.summit.stp.comment.api.client;

import com.summit.stp.comment.api.vo.CommentSimpleVO;
import com.summit.stp.common.application.api.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Comment 微服务 Feign 客户端 - 供其他微服务调用评论查询能力
 */
@FeignClient(name = "stp-comment-service", contextId = "commentFeignClient")
public interface CommentFeignClient {

    /**
     * 根据评论 ID 查询评论简要信息（供其他服务获取评论元数据）
     */
    @GetMapping("/post/comment/internal/simple/{id}")
    Result<CommentSimpleVO> findSimpleCommentById(@PathVariable("id") Long id);

    /**
     * 批量查询评论简要信息（含当前用户点赞状态）
     */
    @PostMapping("/post/comment/internal/simple/batch")
    Result<List<CommentSimpleVO>> findSimpleCommentsByIds(@RequestBody List<Long> ids);
}



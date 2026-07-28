package com.summit.stp.comment.api;

import com.summit.stp.common.application.vo.CommentSimpleVO;
import com.summit.stp.common.result.Result;
import com.summit.stp.comment.application.service.CommentQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评论内部接口 - 供其他微服务通过 Feign 调用
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/post/comment/internal")
public class CommentInternalController {

    private final CommentQueryService commentQueryService;

    /**
     * 查询评论简要信息（供 IM 服务获取评论元数据用于通知）
     */
    @GetMapping("/simple/{id}")
    public Result<CommentSimpleVO> findSimpleCommentById(@PathVariable Long id) {
        return Result.success(commentQueryService.querySimpleCommentWithLikeStatus(id));
    }

    /**
     * 批量查询评论简要信息（含当前用户点赞状态），供互动消息列表查询
     */
    @PostMapping("/simple/batch")
    public Result<List<CommentSimpleVO>> findSimpleCommentsByIds(@RequestBody List<Long> ids) {
        return Result.success(commentQueryService.querySimpleCommentsWithLikeStatus(ids));
    }
}

package com.summit.stp.comment.api;

import com.summit.stp.comment.application.service.CommentAppService;
import com.summit.stp.comment.infrastructure.persistence.po.CommentsPO;
import com.summit.stp.shared.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post/comment")
@RequiredArgsConstructor
@Api(tags = "评论信息管理")
public class CommentController {
    private final CommentAppService commentAppService;

    @GetMapping("/{id}")
    @ApiOperation(value = "获取单条评论详情", notes = "根据评论ID获取对应评论的具体内容")
    public Result<CommentsPO> getComment(
            @ApiParam(value = "评论ID", required = true) @PathVariable Long id) {
        return Result.success(commentAppService.getCommentById(id));
    }

    @PostMapping("/post")
    @ApiOperation(value = "发表/新增评论", notes = "向帖子发表一条新评论或回复已有的评论")
    public Result<Void> postComment(
            @ApiParam(value = "评论具体信息", required = true) @RequestBody CommentsPO comment) {
        commentAppService.postComment(comment);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除评论", notes = "根据评论ID级联删除指定评论")
    public Result<Void> deleteComment(
            @ApiParam(value = "评论ID", required = true) @PathVariable Long id) {
        commentAppService.deleteComment(id);
        return Result.success(null);
    }

    @GetMapping("/post/{postId}")
    @ApiOperation(value = "获取帖子的评论列表", notes = "根据帖子ID，按时间升序获取当前帖子的全部评论数据")
    public Result<List<CommentsPO>> getCommentsByPost(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long postId) {
        return Result.success(commentAppService.getCommentsByPostId(postId));
    }
}

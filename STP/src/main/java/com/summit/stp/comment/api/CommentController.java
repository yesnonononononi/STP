package com.summit.stp.comment.api;

import com.summit.stp.comment.api.dto.request.CommentReplyQueryRequest;
import com.summit.stp.comment.api.dto.request.CreateCommentRequest;
import com.summit.stp.comment.api.dto.request.QueryCommentRequest;
import com.summit.stp.comment.application.command.CommentReplyQueryCommand;
import com.summit.stp.comment.application.command.CreateCommentCommand;
import com.summit.stp.comment.application.command.QueryCommentCommand;
import com.summit.stp.comment.application.service.CommentAppService;
import com.summit.stp.comment.application.service.CommentQueryService;
import com.summit.stp.comment.application.vo.CommentVO;
import com.summit.stp.comment.domain.model.Comment;
import com.summit.stp.comment.infrastructure.persistence.po.CommentsPO;
import com.summit.stp.shared.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.summit.stp.shared.result.CursorPageResult;

@RestController
@RequestMapping("/post/comment")
@RequiredArgsConstructor
@Api(tags = "评论信息管理")
public class CommentController {
    private final CommentAppService commentAppService;
    private final CommentQueryService commentQueryService;

    @PostMapping("/post")
    @ApiOperation(value = "发表/新增评论", notes = "向帖子发表一条新评论或回复已有的评论")
    public Result<CommentVO> postComment(
            @ApiParam(value = "评论具体信息", required = true) @RequestBody CreateCommentRequest createCommentRequest) {
        CreateCommentCommand command = CreateCommentCommand.builder()
                .content(createCommentRequest.getContent())
                .postId(createCommentRequest.getPostId())
                .rootId(createCommentRequest.getRootId())
                .parentId(createCommentRequest.getParentId())
                .extra(createCommentRequest.getExtra())
                .type(createCommentRequest.getType())
                .build();
        Comment comment = commentAppService.postComment(command);
        CommentVO vo = commentQueryService.queryCommentById(comment.getId());
        return Result.success(vo);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除评论", notes = "根据评论ID级联删除指定评论")
    public Result<Void> deleteComment(
            @ApiParam(value = "评论ID", required = true) @PathVariable Long id) {
        commentAppService.deleteComment(id);
        return Result.success();
    }

    @PostMapping("/post/list")
    @ApiOperation(value = "获取帖子的评论列表", notes = "根据帖子ID，按时间升序获取当前帖子的全部评论数据")
    public Result<CursorPageResult<CommentVO>> getCommentsByPost(@RequestBody QueryCommentRequest request)
    {
        QueryCommentCommand command = QueryCommentCommand.builder()
                .idCursor(request.getIdCursor())
                .hsCursor(request.getHsCursor())
                .limit(request.getLimit())
                .postId(request.getPostId())
                .build();
        return Result.success(commentQueryService.queryCommentByPostIdWithCursor(command));
    }

    @PostMapping("/search/reply")
    @ApiOperation(value = "获取帖子下的回复列表", notes = "根据帖子ID，按时间升序获取当前帖子的全部回复数据")
    public Result<CursorPageResult<CommentVO>> getRepliesByPost(
            @ApiParam(value = "帖子ID", required = true) @RequestBody CommentReplyQueryRequest commentReplyQueryRequest
    ) {
        CommentReplyQueryCommand command = CommentReplyQueryCommand.builder()
                .cursor(commentReplyQueryRequest.getCursor())
                .limit(commentReplyQueryRequest.getLimit())
                .postId(commentReplyQueryRequest.getPostId())
                .rootId(commentReplyQueryRequest.getRootId())
                .build();
        return Result.success(commentQueryService.queryReplyByCursor(command));
    }

    @GetMapping("/like/{id}")
    @ApiOperation(value = "点赞")
    public Result<Boolean> like(
            @ApiParam(value = "评论ID", required = true) @PathVariable Long id) {
        return Result.success(commentAppService.like(id));
    }

    @GetMapping("/reply/{id}")
    @ApiOperation(value = "回复(缓存预热)")
    public Result<Void> reply(
            @ApiParam(value = "评论ID", required = true) @PathVariable Long id) {
        commentAppService.reply(id);
        return Result.success();
    }


    @GetMapping("/top/{postId}")
    @ApiOperation(value = "评论置顶")
    public Result<Boolean> top(@RequestParam("commentId") Long id ,@PathVariable Long postId){
        return Result.success(commentAppService.top(id,postId));
    }
}

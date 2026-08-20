package com.summit.stp.comment.comment.api;

import com.summit.stp.comment.comment.api.dto.request.CreateCommentImageRequest;
import com.summit.stp.comment.comment.api.dto.request.UpdateCommentImageRequest;
import com.summit.stp.comment.comment.application.command.CreateCommentImageCommand;
import com.summit.stp.comment.comment.application.command.UpdateCommentImageCommand;
import com.summit.stp.comment.comment.application.service.CommentImageAppService;
import com.summit.stp.comment.comment.application.vo.CommentImageVO;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment/image")
@RequiredArgsConstructor
@Api(tags = "评论图片管理")
public class CommentImageController {
    private final CommentImageAppService commentImageAppService;

    @GetMapping("/{id}")
    @ApiOperation(value = "获取图片详情", notes = "根据图片自增主键ID获取单张图片信息")
    public Result<CommentImageVO> getCommentImage(
            @ApiParam(value = "图片ID", required = true) @PathVariable Long id) {
        return Result.success(commentImageAppService.getCommentImageById(id));
    }

    @GetMapping("/comment/{commentId}")
    @ApiOperation(value = "获取评论的所有图片", notes = "根据评论ID获取该评论下关联的所有图片列表")
    public Result<List<CommentImageVO>> getImagesByCommentId(
            @ApiParam(value = "评论ID", required = true) @PathVariable Long commentId) {
        return Result.success(commentImageAppService.getImagesByCommentId(commentId));
    }

    @Login
    @PostMapping("/create")
    @ApiOperation(value = "添加评论图片", notes = "新建一条评论图片关联记录")
    public Result<Void> createCommentImage(
            @ApiParam(value = "创建评论图片参数", required = true) @RequestBody CreateCommentImageRequest request) {
        CreateCommentImageCommand command = CreateCommentImageCommand.builder()
                .commentId(request.getCommentId())
                .imageUrl(request.getImageUrl())
                .width(request.getWidth())
                .height(request.getHeight())
                .size(request.getSize())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();
        commentImageAppService.createCommentImage(command);
        return Result.success();
    }

    @Login
    @PutMapping("/update")
    @ApiOperation(value = "修改评论图片信息", notes = "根据主键修改图片的URL、排序、宽高、文件大小或状态")
    public Result<Void> updateCommentImage(
            @ApiParam(value = "更新评论图片参数", required = true) @RequestBody UpdateCommentImageRequest request) {
        UpdateCommentImageCommand command = UpdateCommentImageCommand.builder()
                .id(request.getId())
                .imageUrl(request.getImageUrl())
                .width(request.getWidth())
                .height(request.getHeight())
                .size(request.getSize())
                .sortOrder(request.getSortOrder())
                .status(request.getStatus())
                .build();
        commentImageAppService.updateCommentImage(command);
        return Result.success();
    }

    @Login
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除评论图片", notes = "根据主键ID物理删除某张图片记录")
    public Result<Void> deleteCommentImage(
            @ApiParam(value = "图片ID", required = true) @PathVariable Long id) {
        commentImageAppService.deleteCommentImage(id);
        return Result.success();
    }

    @Login
    @DeleteMapping("/comment/{commentId}")
    @ApiOperation(value = "删除评论下的所有图片", notes = "根据评论ID删除该评论关联的所有图片记录")
    public Result<Void> deleteImagesByCommentId(
            @ApiParam(value = "评论ID", required = true) @PathVariable Long commentId) {
        commentImageAppService.deleteImagesByCommentId(commentId);
        return Result.success();
    }
}

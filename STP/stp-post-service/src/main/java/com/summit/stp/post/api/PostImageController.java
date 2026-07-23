package com.summit.stp.post.api;

import com.summit.stp.post.api.dto.request.CreatePostImageRequest;
import com.summit.stp.post.api.dto.request.UpdatePostImageRequest;
import com.summit.stp.post.application.command.CreatePostImageCommand;
import com.summit.stp.post.application.command.UpdatePostImageCommand;
import com.summit.stp.post.application.service.PostImageAppService;
import com.summit.stp.post.application.vo.PostImageVO;
import com.summit.stp.shared.result.Result;
import com.summit.stp.shared.annotation.Login;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post/image")
@RequiredArgsConstructor
@Api(tags = "帖子图片管理")
public class PostImageController {
    private final PostImageAppService postImageAppService;

    @GetMapping("/{id}")
    @ApiOperation(value = "获取图片详情", notes = "根据图片自增主键ID获取单张图片信息")
    public Result<PostImageVO> getPostImage(
            @ApiParam(value = "图片ID", required = true) @PathVariable Long id) {
        return Result.success(postImageAppService.getPostImageById(id));
    }

    @GetMapping("/post/{postId}")
    @ApiOperation(value = "获取帖子的所有图片", notes = "根据帖子ID获取该帖子下关联的所有图片列表")
    public Result<List<PostImageVO>> getImagesByPostId(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long postId) {
        return Result.success(postImageAppService.getImagesByPostId(postId));
    }

    @Login
    @PostMapping("/create")
    @ApiOperation(value = "添加帖子图片", notes = "新建一条帖子图片关联记录")
    public Result<Void> createPostImage(
            @ApiParam(value = "创建帖子图片参数", required = true) @RequestBody CreatePostImageRequest request) {
        CreatePostImageCommand command = CreatePostImageCommand.builder()
                .postId(request.getPostId())
                .imageUrl(request.getImageUrl())
                .width(request.getWidth())
                .height(request.getHeight())
                .size(request.getSize())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();
        postImageAppService.createPostImage(command);
        return Result.success();
    }

    @Login
    @PutMapping("/update")
    @ApiOperation(value = "修改帖子图片信息", notes = "根据主键修改图片的URL、排序、宽高、文件大小或状态")
    public Result<Void> updatePostImage(
            @ApiParam(value = "更新帖子图片参数", required = true) @RequestBody UpdatePostImageRequest request) {
        UpdatePostImageCommand command = UpdatePostImageCommand.builder()
                .id(request.getId())
                .imageUrl(request.getImageUrl())
                .width(request.getWidth())
                .height(request.getHeight())
                .size(request.getSize())
                .sortOrder(request.getSortOrder())
                .status(request.getStatus())
                .build();
        postImageAppService.updatePostImage(command);
        return Result.success();
    }

    @Login
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除帖子图片", notes = "根据主键ID物理删除某张图片记录")
    public Result<Void> deletePostImage(
            @ApiParam(value = "图片ID", required = true) @PathVariable Long id) {
        postImageAppService.deletePostImage(id);
        return Result.success();
    }

    @Login
    @DeleteMapping("/post/{postId}")
    @ApiOperation(value = "删除帖子下的所有图片", notes = "根据帖子ID删除该帖子关联的所有图片记录")
    public Result<Void> deleteImagesByPostId(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long postId) {
        postImageAppService.deleteImagesByPostId(postId);
        return Result.success();
    }
}

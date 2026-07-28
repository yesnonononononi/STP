package com.summit.stp.post.api;


import com.summit.stp.post.api.dto.request.CreatePostRequest;
import com.summit.stp.post.api.dto.request.QueryPostListPageRequest;
import com.summit.stp.post.api.dto.request.UpdatePostRequest;
import com.summit.stp.post.application.command.CreatePostCommand;
import com.summit.stp.post.application.command.QueryPostListByCursorCommand;
import com.summit.stp.post.application.command.UpdatePostCommand;
import com.summit.stp.post.application.service.PostAppService;
import com.summit.stp.post.application.service.PostQueryService;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.common.ThreadContext.UserHolder;
import com.summit.stp.common.result.Result;
import com.summit.stp.common.annotation.Login;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Api(tags = "帖子基本信息管理")
public class PostController {
    private final PostAppService postAppService;
    private final PostQueryService postQueryService;

    @GetMapping("/{id}")
    @ApiOperation(value = "获取帖子详情", notes = "根据帖子ID获取帖子的具体数据")
    public Result<PostVO> getPost(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long id) {
        return Result.success(postAppService.getPostById(id));
    }

    @Login
    @PostMapping("/create")
    @ApiOperation(value = "发布新帖子", notes = "创建并发布一个新帖子")
    public Result<Void> createPost(
            @ApiParam(value = "发帖请求参数", required = true) @RequestBody CreatePostRequest request) {
        CreatePostCommand command = CreatePostCommand.builder()
                .creatorId(UserHolder.getUser().getId())
                .title(request.getTitle())
                .type(request.getType())
                .content(request.getContent())
                .mediaUrls(request.getMediaUrls())
                .status(request.getStatus())
                .tagIds(request.getTagIds())
                .isTop(request.getIsTop())
                .build();
        return postAppService.createPost(command);

    }

    @Login
    @PutMapping("/update")
    @ApiOperation(value = "编辑/更新帖子", notes = "修改已发布帖子的内容或基本字段")
    public Result<Void> updatePost(
            @ApiParam(value = "更新的帖子参数", required = true) @RequestBody UpdatePostRequest request) {
        UpdatePostCommand command = UpdatePostCommand.builder()
                .id(request.getId())
                .creatorId(UserHolder.getUser().getId())
                .title(request.getTitle())
                .type(request.getType())
                .content(request.getContent())
                .mediaUrls(request.getMediaUrls())
                .status(request.getStatus())
                .tagIds(request.getTagIds())
                .isTop(request.getIsTop())
                .build();
        postAppService.updatePost(command);
        return Result.success();
    }

    @Login
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除帖子", notes = "根据帖子ID删除指定帖子")
    public Result<Void> deletePost(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long id) {
        postAppService.deletePost(id);
        return Result.success();
    }

    @Login
    @PutMapping("/republish/{id}")
    @ApiOperation(value = "重新发布已删除的帖子", notes = "将已删除帖子的状态恢复为正常发布状态")
    public Result<Void> republishPost(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long id) {
        postAppService.republishPost(id);
        return Result.success();
    }

    @PostMapping("/page")
    @ApiOperation(value = "分页获取帖子列表", notes = "以分页形式获取全局帖子列表，可按创建时间降序")
    public Result<List<PostVO>> getPostPage(@RequestBody QueryPostListPageRequest request) {
        return Result.success(postAppService.getPostPage(new QueryPostListByCursorCommand(
                request.getCursor(),
                request.getSelf(),
                request.getCreatorId(),
                request.getStatus(),
                request.getOrderType()
        )));
    }

    @Login
    @PostMapping("/like/{id}")
    @ApiOperation(value = "点赞帖子", notes = "点赞/取消点赞指定帖子")
    public Result<Void> likePost(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long id) {
        postAppService.likePost(id);
        return Result.success();
    }

    @Login
    @PostMapping("/collect/{id}")
    @ApiOperation(value = "收藏帖子", notes = "收藏/取消收藏指定帖子")
    public Result<Void> collectPost(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long id) {
        postAppService.collectPost(id);
        return Result.success();
    }

    @PostMapping("/view/{id}")
    @ApiOperation(value = "增加帖子浏览量", notes = "自增指定帖子的浏览次数")
    public Result<Void> viewPost(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long id) {
        postAppService.viewPost(id);
        return Result.success();
    }

    @Login
    @GetMapping("/like/status/{id}")
    @ApiOperation(value = "获取帖子点赞状态", notes = "获取当前登录用户对指定帖子的点赞状态")
    public Result<Boolean> isLiked(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long id) {
        return Result.success(postAppService.isLiked(id));
    }

    @Login
    @GetMapping("/collect/status/{id}")
    @ApiOperation(value = "获取帖子收藏状态", notes = "获取当前登录用户对指定帖子的收藏状态")
    public Result<Boolean> isCollected(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long id) {
        return Result.success(postAppService.isCollected(id));
    }

    @Login
    @GetMapping("/collect/my")
    @ApiOperation(value = "获取用户收藏的帖子列表", notes = "获取用户收藏的帖子列表")
    public Result<List<PostVO>> getMyCollectPostList(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "cursor", required = false) String cursor) {
        return Result.success(postQueryService.getMyCollectPostList(userId, cursor));
    }

    @Login
    @GetMapping("/like/my")
    @ApiOperation(value = "获取用户点赞的帖子列表", notes = "获取用户点赞的帖子列表")
    public Result<List<PostVO>> getMyLikePostList(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "cursor", required = false) String cursor) {
        return Result.success(postQueryService.getMyLikePostList(userId, cursor));
    }

    @Login
    @PutMapping("/top/{id}")
    @ApiOperation(value = "置顶/取消置顶帖子", notes = "设置或取消帖子的置顶状态")
    public Result<Void> topPost(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long id,
            @ApiParam(value = "是否置顶: 0否, 1是", required = true) @RequestParam Integer isTop) {
        postAppService.topPost(id, isTop);
        return Result.success();
    }


    @Login
    @GetMapping("/visible/{id}")
    @ApiOperation(value = "设置帖子可见性", notes = "设置帖子可见性")
    public Result<Void> setVisible(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long id,
            @ApiParam(required = true) @RequestParam Integer visible) {
        postAppService.visibleSelf(id, visible);
        return Result.success();
    }


    @Login
    @GetMapping("/follow/list")
    @ApiOperation(value = "获取我关注的用户的帖子列表")
    public Result<List<PostVO>> getFollowPostList(@RequestBody QueryPostListPageRequest request) {
        return Result.success(postQueryService.getFollowPostList(request));
    }


    @GetMapping("/search/{keyWord}")
    @ApiOperation(value = "帖子搜索")
    public Result<List<PostVO>> searchPost(@PathVariable String keyWord) {
        return Result.success(postQueryService.searchPost(keyWord));
    }


}

package com.summit.stp.post.api;

import com.summit.stp.post.application.service.PostTagRelAppService;
import com.summit.stp.post.application.vo.PostTagRelVO;
import com.summit.stp.shared.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post/relation")
@RequiredArgsConstructor
@Api(tags = "帖子标签关联管理")
public class PostTagRelController {
    private final PostTagRelAppService postTagRelAppService;

    @PostMapping("/bind")
    @ApiOperation(value = "绑定标签到帖子", notes = "为特定帖子分配一个标签记录")
    public Result<Void> bindTag(
            @ApiParam(value = "帖子ID", required = true) @RequestParam Long postId,
            @ApiParam(value = "标签ID", required = true) @RequestParam List<Long> tagIds) {
        postTagRelAppService.bindTag(postId, tagIds);
        return Result.success(null);
    }

    @DeleteMapping("/unbind/{id}")
    @ApiOperation(value = "解绑帖子标签", notes = "解除帖子与标签的关联，根据自增关联ID解绑")
    public Result<Void> unbindTag(
            @ApiParam(value = "关联ID", required = true) @PathVariable Long id) {
        postTagRelAppService.unbindTag(id);
        return Result.success(null);
    }

    @DeleteMapping("/clear/{postId}")
    @ApiOperation(value = "清空帖子的所有标签", notes = "解除该帖子关联的所有标签")
    public Result<Void> clearPostTags(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long postId) {
        postTagRelAppService.clearPostTags(postId);
        return Result.success(null);
    }

    @GetMapping("/post/{postId}")
    @ApiOperation(value = "获取帖子关联的全部标签关系", notes = "获取该帖子绑定的所有关联数据列表")
    public Result< List<PostTagRelVO> > getRelationsByPostId(
            @ApiParam(value = "帖子ID", required = true) @PathVariable Long postId) {
        return Result.success(postTagRelAppService.getRelationsByPostId(postId));
    }

    @GetMapping("/tag/{tagId}")
    @ApiOperation(value = "获取标签关联的全部帖子关系", notes = "根据标签ID反向获取所有绑定的帖子关联数据列表")
    public Result<List<PostTagRelVO>> getRelationsByTagId(
            @ApiParam(value = "标签ID", required = true) @PathVariable Long tagId) {
        return Result.success(postTagRelAppService.getRelationsByTagId(tagId));
    }
}

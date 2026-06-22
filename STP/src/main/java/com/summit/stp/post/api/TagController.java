package com.summit.stp.post.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.post.api.dto.request.CreateTagRequest;
import com.summit.stp.post.api.dto.request.UpdateTagRequest;
import com.summit.stp.post.application.command.UpdateTagCommand;
import com.summit.stp.post.application.service.TagAppService;
import com.summit.stp.post.application.vo.TagVO;
import com.summit.stp.shared.result.Result;
import com.summit.stp.shared.service.SearchSuggest.SuggestVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post/tag")
@RequiredArgsConstructor
@Api(tags = "标签基本信息管理")
public class TagController {
    private final TagAppService tagAppService;

    @GetMapping("/{id}")
    @ApiOperation(value = "获取标签详情", notes = "根据标签ID获取标签的具体数据")
    public Result<TagVO> getTag(
            @ApiParam(value = "标签ID", required = true) @PathVariable Long id) {
        return Result.success(tagAppService.getTagById(id));
    }

    @PostMapping("/create")
    @ApiOperation(value = "创建新标签", notes = "创建一个新标签供帖子使用")
    public Result<Void> createTag(
            @ApiParam(value = "创建标签参数", required = true) @RequestBody CreateTagRequest request) {

        tagAppService.createTag(request.getTagName());
        return Result.success(null);
    }

    @PutMapping("/update")
    @ApiOperation(value = "编辑/更新标签", notes = "修改标签的颜色、名称或权重")
    public Result<Void> updateTag(
            @ApiParam(value = "更新标签参数", required = true) @RequestBody UpdateTagRequest request) {
        UpdateTagCommand command = UpdateTagCommand.builder()
                .id(request.getId())
                .tagName(request.getTagName())
                .sort(request.getSort())
                .status(request.getStatus())
                .build();
        tagAppService.updateTag(command);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除标签", notes = "根据标签ID物理删除指定标签")
    public Result<Void> deleteTag(
            @ApiParam(value = "标签ID", required = true) @PathVariable Long id) {
        tagAppService.deleteTag(id);
        return Result.success(null);
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页获取标签列表", notes = "以分页形式获取标签列表，默认按使用频次降序")
    public Result<Page<TagVO>> getTagPage(
            @ApiParam(value = "页码 (默认1)", required = false) @RequestParam(defaultValue = "1") long page,
            @ApiParam(value = "每页大小 (默认10)", required = false) @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(tagAppService.getTagPage(page, pageSize));
    }
    @GetMapping("/search")
    @ApiOperation(value = "搜索标签建议", notes = "根据标签名称搜索标签")
    public Result<SuggestVO> searchTag(
            @ApiParam(value = "标签名称", required = true) @RequestParam("keyword") String keyword,
            @ApiParam(value = "每页大小 (默认10)", required = false) @RequestParam(defaultValue = "10") Integer limit) {
        return  tagAppService.searchTag(keyword, limit);
    }


    @GetMapping("/recent")
    @ApiOperation(value = "获取最近使用标签", notes = "获取最近使用标签，默认返回10个")
    public Result<List<TagVO>> getRecentTag(
            @ApiParam(value = "每页大小 (默认10)", required = false) @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(tagAppService.getRecentTag(limit));
    }
}

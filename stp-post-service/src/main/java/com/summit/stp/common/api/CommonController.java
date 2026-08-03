package com.summit.stp.common.api;

import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.api.dto.response.UploadVO;
import com.summit.stp.common.application.service.CommonAppService;
import com.summit.stp.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Login
@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
@Api(tags = "公共服务管理")
public class CommonController {
    private final CommonAppService commonAppService;

    @PostMapping("/upload")
    @ApiOperation(value = "媒体文件上传", notes = "支持图片(最大10MB)和视频(最大50MB)的上传")
    public Result<UploadVO> uploadFile(
            @ApiParam(value = "媒体文件", required = true) @RequestParam("file") MultipartFile file, @RequestParam("folder") String folder) {
        return Result.success(commonAppService.upload(file, folder));
    }


    @PostMapping("/upload/chunk")
    @ApiOperation(value = "大文件分片上传与合并")
    public Result<UploadVO> uploadChunk(
            @ApiParam(value = "分片媒体文件(合并阶段可为 null)") @RequestParam(value = "file", required = false) MultipartFile file,
            @ApiParam(value = "文件夹路径/上传任务ID", required = true) @RequestParam("folder") String folder,
            @ApiParam(value = "当前分片索引，当索引等于总分片数时触发合并", required = true) @RequestParam("curIndex") Integer curIndex,
            @ApiParam(value = "总分片数量", required = true) @RequestParam("chunk") Integer chunk) {
        return commonAppService.uploadDealBigFile(file, folder, curIndex, chunk);
    }
}

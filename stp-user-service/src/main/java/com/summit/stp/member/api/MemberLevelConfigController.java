package com.summit.stp.member.api;

import com.summit.stp.common.result.Result;
import com.summit.stp.member.api.dto.LevelConfigSaveRequest;
import com.summit.stp.member.application.service.MemberLevelConfigAppService;
import com.summit.stp.member.application.vo.MemberLevelConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member/level")
@RequiredArgsConstructor
@Api(tags = "会员等级配置管理")
public class MemberLevelConfigController {

    private final MemberLevelConfigAppService appService;

    //@PostMapping("/save")
    @ApiOperation(value = "保存或更新会员等级配置", notes = "传入等级配置信息进行保存，如果该等级数值已存在则执行更新")
    public Result<Void> saveOrUpdate(@RequestBody LevelConfigSaveRequest request) {
        return appService.saveOrUpdate(request);
    }

    @GetMapping("/get/{level}")
    @ApiOperation(value = "根据等级数值查询配置", notes = "获取指定会员等级的详细配置数据")
    public Result<MemberLevelConfigVO> queryByLevel(
            @ApiParam(value = "会员等级数值", required = true, example = "1") @PathVariable Long level) {
        return appService.queryByLevel(level);
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取所有会员等级配置", notes = "列表展示系统中所有的会员等级配置信息")
    public Result<List<MemberLevelConfigVO>> listAll() {
        return appService.listAll();
    }

    //@DeleteMapping("/delete/{level}")
    @ApiOperation(value = "根据等级数值删除配置", notes = "删除指定会员等级的配置")
    public Result<Void> deleteByLevel(
            @ApiParam(value = "会员等级数值", required = true, example = "1") @PathVariable Long level) {
        return appService.deleteByLevel(level);
    }
}

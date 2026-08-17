package com.summit.stp.member.api;

import com.summit.stp.common.application.api.result.Result;
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



    @GetMapping("/get/{level}")
    @ApiOperation(value = "根据等级数值查询配置", notes = "获取指定会员等级的详细配置数据")
    public Result<MemberLevelConfigVO> queryByLevel(
            @ApiParam(value = "会员等级数值", required = true, example = "1") @PathVariable Long level) {
        return appService.queryByLevel(level);
    }



}

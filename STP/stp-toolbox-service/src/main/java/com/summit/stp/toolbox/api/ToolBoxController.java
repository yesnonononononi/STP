package com.summit.stp.toolbox.api;

import com.summit.stp.common.ThreadContext.UserHolder;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.result.Result;
import com.summit.stp.toolbox.api.dto.SignInInfoVO;
import com.summit.stp.toolbox.application.UserSignStatsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/toolbox")
@RequiredArgsConstructor
@Api(tags = "工具箱及签到管理")
public class ToolBoxController {
    private final UserSignStatsService userSignStatsService;

    @Login
    @GetMapping("/signin/status/{month}")
    @ApiOperation(value = "获取签到打卡状态", notes = "根据流水日志与统计表聚合生成签到状态")
    public Result<SignInInfoVO> getSignInStatus(@PathVariable Integer month) {
        Long userId = UserHolder.getUser().getId();
        log.info("【ToolBox】用户[{}]获取签到状态", userId);
        SignInInfoVO status = userSignStatsService.getSignInInfoVO(userId,month);
        return Result.success(status);
    }

    @Login
    @PostMapping("/signin")
    @ApiOperation(value = "签到打卡", notes = "当前登录用户进行每日打卡签到")
    public Result<Void> doSignIn() {
        Long userId = UserHolder.getUser().getId();
        log.info("【ToolBox】用户[{}]执行签到打卡", userId);
        userSignStatsService.doSignIn(userId);
        return Result.success();
    }
}

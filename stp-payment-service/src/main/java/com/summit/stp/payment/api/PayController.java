package com.summit.stp.payment.api;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.payment.api.dto.PayCallbackRequest;
import com.summit.stp.payment.application.command.PayCallbackCheckCommand;
import com.summit.stp.payment.application.service.PayAppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/pay")
@RequiredArgsConstructor
@Api(tags = "支付管理")
public class PayController {
    private final PayAppService payAppService;

    @GetMapping("/check")
    @ApiOperation(value = "支付成功回调校验与处理", notes = "供三方支付平台进行支付结果的异步通知回调校验")
    public Result<String> checkResult(
            @ApiParam(value = "支付回调请求通知参数", required = true) PayCallbackRequest payResult){
        log.info("收到回调请求: {}", payResult);
        PayCallbackCheckCommand payCallbackCheckCommand = new PayCallbackCheckCommand();
        BeanUtils.copyProperties(payResult, payCallbackCheckCommand);

        payAppService.checkResult(payCallbackCheckCommand);
        return Result.success("success");
    }

    @GetMapping("/types")
    @ApiOperation(value = "获取支持的支付渠道列表", notes = "获取系统当前支持的所有支付方式名称（如 alipay 等）")
    public Result<List<String>> getType(){
        return payAppService.getType();
    }

}

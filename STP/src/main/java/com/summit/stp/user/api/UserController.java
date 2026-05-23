package com.summit.stp.user.api;

import com.summit.stp.shared.result.Result;
import com.summit.stp.user.api.dto.request.UserPutRequest;
import com.summit.stp.user.application.UserApplicationService;
import com.summit.stp.user.application.command.UserPutCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Api(tags = "用户基本信息管理")
public class UserController {
    private final UserApplicationService userApplicationService;

    @PostMapping("/put")
    @ApiOperation(value = "修改用户信息", notes = "修改用户名、密码或绑定手机号等基本资料")
    public Result<Void> put(
            @ApiParam(value = "修改用户信息请求体", required = true) @RequestBody UserPutRequest request){
        UserPutCommand command = UserPutCommand.builder()
                .uname(request.getUname())
                .oldPassword(request.getOldPassword())
                .newPassword(request.getNewPassword())
                .phoneNumber(request.getPhoneNumber())
                .statusCode(request.getStatusCode())
                .verifyCode(request.getVerifyCode())
                .build();
        userApplicationService.put(command);
        return Result.success();
    }
}

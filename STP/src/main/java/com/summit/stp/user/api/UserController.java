package com.summit.stp.user.api;

import com.summit.stp.shared.result.Result;
import com.summit.stp.user.api.dto.request.UserPutRequest;
import com.summit.stp.user.application.UserApplicationService;
import com.summit.stp.user.application.command.UserPutCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserApplicationService userApplicationService;

    @PostMapping("/put")
    public Result<Void> put(@RequestBody  UserPutRequest request){
        UserPutCommand command = UserPutCommand.builder()
                .uname(request.getUname())
                .phoneNumber(request.getPhoneNumber())
                .statusCode(request.getStatusCode())
                .build();
        userApplicationService.put(command);
        return Result.success();
    }



}

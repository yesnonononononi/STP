package com.summit.stp.user.api;

import com.summit.stp.user.api.vo.UserAuthVO;
import com.summit.stp.user.api.vo.UserProfileVO;
import com.summit.stp.user.api.vo.UserSettingVO;
import com.summit.stp.user.api.vo.UserSimpleVO;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.user.application.command.CreateUserCommand;
import com.summit.stp.user.application.service.UserApplicationService;
import com.summit.stp.user.application.service.UserAuthSupport;
import com.summit.stp.user.application.service.UserSettingAppService;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

/**
 * 用户内部接口 - 供其他微服务通过 Feign 调用
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/internal")
public class UserInternalController {

    private final UserRepository<User> userRepository;
    private final UserApplicationService userApplicationService;
    private final UserSettingAppService userSettingAppService;
    private final UserAuthSupport userAuthSupport;

    /**
     * 更新用户登录 IP 归属地（供 auth-service 登录时调用）
     */
    @PostMapping("/updateIp")
    public Result<Void> updateUserIp(@RequestParam Long userId, @RequestParam String ip) {
        try {
            User user = userRepository.findUserById(userId).orElse(null);
            if (user != null) {
                user.updateIp(ip);
                userRepository.updateById(user);
            }
            return Result.success();
        } catch (Exception e) {
            log.error("【用户内部接口】更新用户 IP 失败, userId={}", userId, e);
            return Result.error("更新 IP 失败");
        }
    }

    @GetMapping("/{id}")
    public Result<UserProfileVO> findUserById(@PathVariable Long id) {
        return Result.success(userApplicationService.findUserById(id));
    }

    @GetMapping("/simple/{id}")
    public Result<UserSimpleVO> findSimpleUserById(@PathVariable Long id) {
        return Result.success(userApplicationService.findSimpleUserById(id));
    }

    @PostMapping("/simple/batch")
    public Result<Map<Long, UserSimpleVO>> findSimpleUserByIds(@RequestBody Collection<Long> userIds) {
        return Result.success(userApplicationService.findSimpleUserByIds(userIds));
    }

    @GetMapping("/setting/get/{userId}")
    public Result<UserSettingVO> getUserSetting(@PathVariable Long userId) {
        return Result.success(userSettingAppService.getUserSetting(userId));
    }

    @GetMapping("/user/internal/setting/get/phone/{phone}")
    public Result<UserProfileVO> findUserByPhone(@PathVariable String phone) {
        return Result.success(userApplicationService.findUserByPhone(phone));
    }

    @GetMapping("/user/internal/setting/get/username/{uname}")
    public Result<UserProfileVO> findUserByUname(@PathVariable String uname) {
        return Result.success(userApplicationService.findUserByUname(uname));
    }

    @GetMapping("/register")
    public Result<Void> register(@RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("encryptedValue") String encryptedValue) {
        userApplicationService
                .register(CreateUserCommand.builder().phoneNumber(phoneNumber).password(encryptedValue).build());
        return Result.success();
    }

    /**
     * 认证支持接口 - 根据用户名获取认证信息（包含密码）
     */
    @GetMapping("/auth/by-username/{username}")
    public Result<UserAuthVO> getUserAuthByUsername(@PathVariable String username) {
        UserAuthVO authVO = userAuthSupport.getUserAuthByUsername(username);
        if (authVO == null) {
            return Result.error("用户不存在");
        }
        return Result.success(authVO);
    }

    /**
     * 认证支持接口 - 根据手机号获取认证信息（包含密码）
     */
    @GetMapping("/auth/by-phone/{phone}")
    public Result<UserAuthVO> getUserAuthByPhone(@PathVariable String phone) {
        UserAuthVO authVO = userAuthSupport.getUserAuthByPhone(phone);
        if (authVO == null) {
            return Result.error("用户不存在");
        }
        return Result.success(authVO);
    }

    /**
     * 认证支持接口 - 根据用户ID获取认证信息（包含密码）
     */
    @GetMapping("/auth/by-id/{userId}")
    public Result<UserAuthVO> getUserAuthById(@PathVariable Long userId) {
        UserAuthVO authVO = userAuthSupport.getUserAuthById(userId);
        if (authVO == null) {
            return Result.error("用户不存在");
        }
        return Result.success(authVO);
    }
}

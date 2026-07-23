package com.summit.stp.user.api;

import com.summit.stp.shared.application.vo.UserSimpleVO;
import com.summit.stp.shared.result.Result;
import com.summit.stp.user.application.UserApplicationService;
import com.summit.stp.user.application.service.UserSettingAppService;
import com.summit.stp.user.application.vo.UserProfileVO;
import com.summit.stp.user.application.vo.UserSettingVO;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    private final UserRepository userRepository;
    private final UserApplicationService userApplicationService;
    private final UserSettingAppService userSettingAppService;

    /**
     * 更新用户登录 IP 归属地（供 auth-service 登录时调用）
     */
    @PostMapping("/updateIp")
    public Result<Void> updateUserIp(@RequestParam Long userId, @RequestParam String ip) {
        try {
            User user = userRepository.findUserById(userId);
            if (user != null) {
                user.updateIp(ip);
                userRepository.save(user);
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
}

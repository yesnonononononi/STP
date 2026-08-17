package com.summit.stp.user.api.client;

import com.summit.stp.user.api.vo.AuthVO;
import com.summit.stp.user.api.vo.UserProfileVO;
import com.summit.stp.user.api.vo.UserSettingVO;
import com.summit.stp.user.api.vo.UserSimpleVO;
import com.summit.stp.common.application.api.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@FeignClient(name = "stp-user-service", contextId = "userFeignClient")
public interface UserFeignClient {

    @GetMapping("/user/internal/{id}")
    Result<UserSimpleVO> findUserById(@PathVariable("id") Long id);

    @GetMapping("/user/internal/simple/{id}")
    Result<UserSimpleVO> findSimpleUserById(@PathVariable("id") Long id);

    @PostMapping("/user/internal/simple/batch")
    Result<Map<Long, UserSimpleVO>> findSimpleUserByIds(@RequestBody Collection<Long> userIds);

    @GetMapping("/user/internal/setting/get/{userId}")
    Result<UserSettingVO> getUserSetting(@PathVariable Long userId);

    @GetMapping("/user/internal/setting/get/phone/{phone}")
    Result<UserProfileVO> findUserByPhone(@PathVariable String phone);

    @GetMapping("/user/internal/setting/get/username/{uname}")
    Result<UserProfileVO> findUserByUname(@PathVariable String uname);

    /**
     * 更新用户登录 IP 归属地（供 auth-service 登录时调用）
     */
    @PostMapping("/user/internal/updateIp")
    Result<Void> updateUserIp(@RequestParam("userId") Long userId, @RequestParam("ip") String ip);

    @GetMapping("/user/internal/register")
    Result<Void> register(@RequestParam("phoneNumber") String phoneNumber, @RequestParam("encryptedValue") String encryptedValue);

}



package com.summit.stp.common.feign;

import com.summit.stp.common.application.api.vo.UserProfileVO;
import com.summit.stp.common.application.api.vo.UserSettingVO;
import com.summit.stp.common.application.api.vo.UserSimpleVO;
import com.summit.stp.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@FeignClient(name = "stp-user-service", contextId = "userFeignClient")
public interface UserFeignClient {

    @GetMapping("/user/internal/{id}")
    Result<UserProfileVO> findUserById(@PathVariable("id") Long id);

    @GetMapping("/user/internal/simple/{id}")
    Result<UserSimpleVO> findSimpleUserById(@PathVariable("id") Long id);

    @PostMapping("/user/internal/simple/batch")
    Result<Map<Long, UserSimpleVO>> findSimpleUserByIds(@RequestBody Collection<Long> userIds);

    @GetMapping("/user/internal/setting/get/{userId}")
    Result<UserSettingVO> getUserSetting(@PathVariable("userId") Long userId);

    /**
     * 更新用户登录 IP 归属地（供 auth-service 登录时调用）
     */
    @PostMapping("/user/internal/updateIp")
    Result<Void> updateUserIp(@RequestParam("userId") Long userId, @RequestParam("ip") String ip);
}

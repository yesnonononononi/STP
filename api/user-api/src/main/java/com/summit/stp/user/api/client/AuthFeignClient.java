package com.summit.stp.user.api.client;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.user.api.vo.UserAuthVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 认证专用 Feign Client - 供 auth-service 调用
 * 与 UserFeignClient 隔离，专门处理认证相关的用户数据访问
 */
@FeignClient(name = "stp-user-service", contextId = "authFeignClient")
public interface AuthFeignClient {

    /**
     * 根据用户名获取认证信息（包含密码）
     */
    @GetMapping("/user/internal/auth/by-username/{username}")
    Result<UserAuthVO> getUserAuthByUsername(@PathVariable("username") String username);

    /**
     * 根据手机号获取认证信息（包含密码）
     */
    @GetMapping("/user/internal/auth/by-phone/{phone}")
    Result<UserAuthVO> getUserAuthByPhone(@PathVariable("phone") String phone);

    /**
     * 根据用户ID获取认证信息（包含密码）
     */
    @GetMapping("/user/internal/auth/by-id/{userId}")
    Result<UserAuthVO> getUserAuthById(@PathVariable("userId") Long userId);
}

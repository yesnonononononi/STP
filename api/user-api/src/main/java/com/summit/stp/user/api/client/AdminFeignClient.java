package com.summit.stp.user.api.client;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "stp-user-service",contextId = "adminFeignClient")
public interface AdminFeignClient {

    /**
     * 判断用户是否是管理员
     *
     * @param uid 用户ID
     * @return null : 不是管理员
     * >= 0: 管理员等级
     */
    @GetMapping("/u/admin/internal/is/{uid}")
    Integer isAdmin(@PathVariable Long uid);
}


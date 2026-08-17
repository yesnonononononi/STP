package com.summit.stp.user.application.service;

import com.summit.stp.user.api.vo.UserAuthVO;

/**
 * 用户认证支持服务接口
 * 专门为认证服务提供用户数据，与主业务服务层隔离
 */
public interface UserAuthSupport {

    /**
     * 根据用户名获取认证信息
     * @param username 用户名
     * @return 认证信息VO（包含密码）
     */
    UserAuthVO getUserAuthByUsername(String username);

    /**
     * 根据手机号获取认证信息
     * @param phone 手机号
     * @return 认证信息VO（包含密码）
     */
    UserAuthVO getUserAuthByPhone(String phone);

    /**
     * 根据用户ID获取认证信息
     * @param userId 用户ID
     * @return 认证信息VO（包含密码）
     */
    UserAuthVO getUserAuthById(Long userId);
}

package com.summit.stp.user.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.user.api.vo.UserProfileVO;
import com.summit.stp.user.api.vo.UserSimpleVO;
import com.summit.stp.user.application.command.CreateUserCommand;
import com.summit.stp.user.application.command.UserPasswordUpdateCommand;
import com.summit.stp.user.application.command.UserPhoneBindCommand;
import com.summit.stp.user.application.command.UserProfileUpdateCommand;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface UserApplicationService {
    void updateProfile(UserProfileUpdateCommand command);
    void bindPhone(UserPhoneBindCommand command);
    void updatePassword(UserPasswordUpdateCommand command);

    /**
     * 根据用户ID查询用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    UserProfileVO findUserById(Long id);

    /**
     * 根据用户ID获取简单展示信息
     * @param id 用户ID
     * @return 用户简单公开信息
     */
    UserSimpleVO findSimpleUserById(Long id);

    Map<Long, UserSimpleVO> findSimpleUserByIds(Collection<Long> userIds);

    List<UserProfileVO> findProfileByIds(List<Long> ids);

    /**
     * 注册用户
     * @param build 注册信息
     */
    void register(CreateUserCommand build);

    UserProfileVO findUserByPhone(String phone);

    UserProfileVO findUserByUname(String uname);

    Page<UserProfileVO> findPage(Integer page, Integer size);
}


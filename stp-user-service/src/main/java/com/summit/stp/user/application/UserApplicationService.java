package com.summit.stp.user.application;

import com.summit.stp.common.application.api.vo.UserProfileVO;
import com.summit.stp.common.application.api.vo.UserSimpleVO;
import com.summit.stp.user.application.command.UserPasswordUpdateCommand;
import com.summit.stp.user.application.command.UserPhoneBindCommand;
import com.summit.stp.user.application.command.UserProfileUpdateCommand;

import java.util.Collection;
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
}

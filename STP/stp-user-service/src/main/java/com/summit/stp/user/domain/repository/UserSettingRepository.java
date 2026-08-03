package com.summit.stp.user.domain.repository;

import com.summit.stp.user.domain.model.UserSetting;

public interface UserSettingRepository {
    /**
     * 保存或更新用户偏好配置
     */
    void save(UserSetting userSetting);

    /**
     * 根据用户ID查询配置
     */
    UserSetting findByUserId(Long userId);
}

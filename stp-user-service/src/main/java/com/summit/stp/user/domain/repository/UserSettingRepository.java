package com.summit.stp.user.domain.repository;

import java.util.Optional;

public interface UserSettingRepository<T> {
    /**
     * 保存用户偏好配置
     */
    void save(T userSetting);

    /**
     * 更新用户偏好配置
     */
    void update(T userSetting);

    /**
     * 根据用户ID查询配置
     */
    Optional<T> findByUserId(Long userId);
}

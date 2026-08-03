package com.summit.stp.user.application.service;

import com.summit.stp.common.application.api.vo.UserSettingVO;
import com.summit.stp.user.application.command.UpdateUserSettingCommand;

public interface UserSettingAppService {
    /**
     * 获取用户设置，如果不存在则自动初始化默认值
     */
    UserSettingVO getUserSetting(Long userId);

    /**
     * 更新用户设置偏好
     */
    void updateSetting(UpdateUserSettingCommand command);
}

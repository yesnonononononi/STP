package com.summit.stp.user.application.service.impl;

import cn.hutool.core.util.IdUtil;
import com.summit.stp.user.api.vo.UserSettingVO;
import com.summit.stp.user.application.command.UpdateUserSettingCommand;
import com.summit.stp.user.application.service.UserSettingAppService;
import com.summit.stp.user.domain.model.UserSetting;
import com.summit.stp.user.domain.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSettingAppServiceImpl implements UserSettingAppService {
    private final UserSettingRepository<UserSetting> userSettingRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserSettingVO getUserSetting(Long userId) {
        UserSetting userSetting = userSettingRepository.findByUserId(userId).orElse(null);
        if (userSetting == null) {
            log.info("【用户配置模块】用户ID:{} 动作：初始化默认配置", userId);
            userSetting = UserSetting.builder()
                    .id(IdUtil.getSnowflakeNextId())
                    .userId(userId)
                    .showDelPost(1)
                    .customizationRecommend(1)
                    .build();
            userSettingRepository.save(userSetting);
        }
        return toVO(userSetting);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSetting(UpdateUserSettingCommand command) {
        Long userId = command.getUserId();
        UserSetting userSetting = userSettingRepository.findByUserId(userId).orElse(null);
        if (userSetting == null) {
            log.info("【用户配置模块】用户ID:{} 动作：更新配置时未找到原配置，执行自动初始化", userId);
            userSetting = UserSetting.builder()
                    .id(IdUtil.getSnowflakeNextId())
                    .userId(userId)
                    .showDelPost(1)
                    .customizationRecommend(1)
                    .build();
            userSetting.update(command.getShowDelPost(), command.getCustomizationRecommend());
            userSettingRepository.save(userSetting);
        } else {
            userSetting.update(command.getShowDelPost(), command.getCustomizationRecommend());
            userSettingRepository.update(userSetting);
        }
        log.info("【用户配置模块】用户ID:{} 动作：更新用户配置成功", userId);
    }

    private UserSettingVO toVO(UserSetting userSetting) {
        return UserSettingVO.builder()
                .userId(userSetting.getUserId())
                .showDelPost(userSetting.getShowDelPost())
                .customizationRecommend(userSetting.getCustomizationRecommend())
                .build();
    }
}

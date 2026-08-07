package com.summit.stp.user.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.user.domain.model.UserSetting;
import com.summit.stp.user.domain.repository.UserSettingRepository;
import com.summit.stp.user.infrastructure.persistence.mapper.UserSettingMapper;
import com.summit.stp.user.infrastructure.persistence.po.UserSettingPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserSettingRepositoryImpl implements UserSettingRepository {
    private final UserSettingMapper userSettingMapper;

    @Override
    public void save(UserSetting userSetting) {
        UserSettingPO po = toPO(userSetting);
        UserSettingPO existing = userSettingMapper.selectOne(new LambdaQueryWrapper<UserSettingPO>()
                .eq(UserSettingPO::getUserId, po.getUserId()));
        if (existing != null) {
            po.setId(existing.getId());
            if (po.getPublicId() == null) {
                po.setPublicId(existing.getPublicId());
            }
            int i = userSettingMapper.updateById(po);
            if (i == 0) {
                log.warn("【用户配置模块】用户ID:{} 动作：更新用户偏好配置失败", userSetting.getUserId());
            }
        } else {
            userSettingMapper.insert(po);
        }
    }

    @Override
    public UserSetting findByUserId(Long userId) {
        UserSettingPO po = userSettingMapper.selectOne(new LambdaQueryWrapper<UserSettingPO>()
                .eq(UserSettingPO::getUserId, userId));
        if (po == null) {
            return null;
        }
        return fromPO(po);
    }

    private UserSettingPO toPO(UserSetting userSetting) {
        UserSettingPO po = new UserSettingPO();
        po.setPublicId(userSetting.getId());
        po.setUserId(userSetting.getUserId());
        po.setShowDelPost(userSetting.getShowDelPost());
        po.setCustomizationRecommend(userSetting.getCustomizationRecommend());
        po.setCreateTime(userSetting.getCreateTime());
        po.setUpdateTime(userSetting.getUpdateTime());
        return po;
    }

    private UserSetting fromPO(UserSettingPO po) {
        return UserSetting.builder()
                .id(po.getPublicId())
                .userId(po.getUserId())
                .showDelPost(po.getShowDelPost())
                .customizationRecommend(po.getCustomizationRecommend())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}
